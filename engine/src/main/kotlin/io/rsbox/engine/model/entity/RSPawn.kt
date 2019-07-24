package io.rsbox.engine.model.entity

import io.rsbox.api.*
import io.rsbox.api.entity.Pawn
import io.rsbox.engine.action.NpcDeathAction
import io.rsbox.engine.action.PlayerDeathAction
import io.rsbox.engine.event.Event
import io.rsbox.engine.message.impl.SetMapFlagMessage
import io.rsbox.engine.model.*
import io.rsbox.engine.model.bits.INFINITE_VARS_STORAGE
import io.rsbox.engine.model.bits.InfiniteVarsType
import io.rsbox.engine.model.collision.CollisionManager
import io.rsbox.engine.model.combat.DamageMap
import io.rsbox.engine.model.path.FutureRoute
import io.rsbox.engine.model.path.PathFindingStrategy
import io.rsbox.engine.model.path.PathRequest
import io.rsbox.engine.model.path.Route
import io.rsbox.engine.model.path.strategy.BFSPathFindingStrategy
import io.rsbox.engine.model.path.strategy.NpcPathFindingStrategy
import io.rsbox.engine.model.queue.QueueTaskSet
import io.rsbox.api.TaskPriority
import io.rsbox.engine.model.queue.QueueTask
import io.rsbox.engine.model.queue.impl.PawnQueueTaskSet
import io.rsbox.engine.model.region.Chunk
import io.rsbox.engine.model.timer.FROZEN_TIMER
import io.rsbox.engine.model.timer.RESET_PAWN_FACING_TIMER
import io.rsbox.engine.model.timer.STUN_TIMER
import io.rsbox.engine.model.timer.TimerMap
import io.rsbox.engine.oldplugin.Plugin
import io.rsbox.engine.service.log.LoggerService
import io.rsbox.engine.sync.block.UpdateBlockBuffer
import io.rsbox.engine.sync.block.UpdateBlockType
import kotlinx.coroutines.CoroutineScope
import java.lang.ref.WeakReference
import java.util.ArrayDeque
import java.util.Queue

/**
 * A controllable character in the world that is used by something, or someone,
 * for their own purpose.
 *
 * @author Tom <rspsmods@gmail.com>
 */
abstract class RSPawn(val world: RSWorld) : RSEntity(), Pawn {

    /**
     * The index assigned when this [RSPawn] is successfully added to a [PawnList].
     */
    var index = -1

    /**
     * @see UpdateBlockBuffer
     */
    internal var blockBuffer = UpdateBlockBuffer()

    /**
     * The 3D [RSTile] that this pawn was standing on, in the last game cycle.
     */
    internal var lastTile: RSTile? = null

    /**
     * The last tile that was set for the pawn's [io.rsbox.engine.model.region.Chunk].
     */
    internal var lastChunkTile: RSTile? = null

    /**
     * Whether or not this pawn can teleported this game cycle.
     */
    internal var moved = false

    /**
     * @see [MovementQueue]
     */
    internal val movementQueue by lazy { MovementQueue(this) }

    /**
     * The current directions that this pawn is moving.
     */
    internal var steps: MovementQueue.StepDirection? = null

    /**
     * The last [Direction] this pawn was facing.
     */
    internal var lastFacingDirection: Direction = Direction.SOUTH

    /**
     * A public getter property for [lastFacingDirection].
     */
    val faceDirection: Direction
        get() = lastFacingDirection

    /**
     * The current [LockState] which filters what actions this pawn can perform.
     */
    var lock = LockState.NONE

    /**
     * The attributes attached to the pawn.
     *
     * @see AttributeMap
     */
    val attr = AttributeMap()

    /**
     * The timers attached to the pawn.
     *
     * @see TimerMap
     */
    val timers = TimerMap()

    internal val queues: QueueTaskSet = PawnQueueTaskSet()

    /**
     * The equipment bonus for the pawn.
     */
    val equipmentBonuses = IntArray(14)

    /**
     * The current prayer icon that the pawn has active.
     */
    var prayerIcon = -1

    /**
     * Transmog is the action of turning into an npc. This value is equal to the
     * npc id of the npc you want to turn into, visually.
     */
    private var transmogId = -1

    /**
     * A list of pending [Hit]s.
     */
    private val pendingHits = mutableListOf<Hit>()

    /**
     * A [DamageMap] to keep track of who has dealt damage to this pawn.
     */
    val damageMap = DamageMap()

    /**
     * A flag which indicates if this pawn is visible to players in the world.
     */
    var invisible = false

    /**
     * The [FutureRoute] for the pawn, if any.
     * @see createPathFindingStrategy
     */
    private var futureRoute: FutureRoute? = null

    /**
     * Boolean whether or not the player can drop items
     */
    var canDropItems: Boolean = true

    /**
     * Boolean wheter or not the player can initiate a trade.
     */
    var canTrade: Boolean = true

    /**
     * Handles logic before any synchronization tasks are executed.
     */
    abstract fun cycle()

    fun isDead(): Boolean = getCurrentHp() == 0

    fun isAlive(): Boolean = !isDead()

    abstract fun isRunning(): Boolean

    abstract fun getSize(): Int

    abstract fun getCurrentHp(): Int

    abstract fun getMaxHp(): Int

    abstract fun setCurrentHp(level: Int)

    abstract fun addBlock(block: UpdateBlockType)

    abstract fun hasBlock(block: UpdateBlockType): Boolean

    /**
     * Lock the pawn to the default [LockState.FULL] state.
     */
    fun lock() {
        lock = LockState.FULL
    }

    /**
     * Unlock the pawn and set it to [LockState.NONE] state.
     */
    fun unlock() {
        lock = LockState.NONE
    }

    /**
     * Checks if the pawn has any lock state set.
     */
    fun isLocked(): Boolean = lock != LockState.NONE

    fun getTransmogId(): Int = transmogId

    fun setTransmogId(transmogId: Int) {
        this.transmogId = transmogId
        addBlock(UpdateBlockType.APPEARANCE)
    }

    fun hasMoveDestination(): Boolean = futureRoute != null || movementQueue.hasDestination()

    fun stopMovement() {
        movementQueue.clear()
    }

    fun getCentreTile(): RSTile = tile.transform(getSize() shr 1, getSize() shr 1) as RSTile

    /**
     * Gets the tile the pawn is currently facing towards.
     */
    // Credits: Kris#1337
    fun getFrontFacingTile(target: RSTile, offset: Int = 0): RSTile {
        val size = (getSize() shr 1)
        val centre = getCentreTile()

        val granularity = 2048
        val lutFactor = (granularity / (Math.PI * 2)) // Lookup table factor

        val theta = Math.atan2((target.z - centre.z).toDouble(), (target.x - centre.x).toDouble())
        var angle = Math.toDegrees((((theta * lutFactor).toInt() + offset) and (granularity - 1)) / lutFactor)
        if (angle < 0) {
            angle += 360
        }
        angle = Math.toRadians(angle)

        val tx = Math.round(centre.x + (size * Math.cos(angle))).toInt()
        val tz = Math.round(centre.z + (size * Math.sin(angle))).toInt()
        return RSTile(tx, tz, tile.height)
    }

    /**
     * Alias for [getFrontFacingTile] using a [RSPawn] as the target tile.
     */
    fun getFrontFacingTile(target: RSPawn, offset: Int = 0): RSTile = getFrontFacingTile(target.getCentreTile(), offset)

    /**
     * Initiate combat with [target].
     */
    fun attack(target: RSPawn) {
        resetInteractions()
        interruptQueues()

        attr[COMBAT_TARGET_FOCUS_ATTR] = WeakReference(target as Pawn)

        /*
         * Players always have the default combat, and npcs will use default
         * combat <strong>unless</strong> they have a custom npc combat oldplugin
         * bound to their npc id.
         */
        if (entityType.isPlayer || this is RSNpc && !world.plugins.executeNpcCombat(this)) {
            world.plugins.executeCombat(this)
        }
    }

    fun addHit(hit: Hit) {
        pendingHits.add(hit)
    }

    fun clearHits() {
        pendingHits.clear()
    }

    /**
     * Handle a single cycle for [timers].
     */
    fun timerCycle() {
        val iterator = timers.getTimers().iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val key = entry.key
            val time = entry.value
            if (time <= 0) {
                if (key == RESET_PAWN_FACING_TIMER) {
                    resetFacePawn()
                } else {
                    world.plugins.executeTimer(this, key)
                }
                if (!timers.has(key)) {
                    iterator.remove()
                }
            }
        }

        timers.getTimers().entries.forEach { timer ->
            timer.setValue(timer.value - 1)
        }
    }

    /**
     * Handle a single cycle for [pendingHits].
     */
    fun hitsCycle() {
        val hitIterator = pendingHits.iterator()
        iterator@ while (hitIterator.hasNext()) {
            if (isDead()) {
                break
            }
            val hit = hitIterator.next()

            if (lock.delaysDamage()) {
                hit.damageDelay = Math.max(0, hit.damageDelay - 1)
                continue
            }

            if (hit.damageDelay-- == 0) {
                if (!hit.cancelCondition()) {
                    blockBuffer.hits.add(hit)
                    addBlock(UpdateBlockType.HITMARK)

                    for (hitmark in hit.hitmarks) {
                        val hp = getCurrentHp()
                        if (hitmark.damage > hp) {
                            hitmark.damage = hp
                        }
                        /*
                         * Only lower the pawn's hp if they do not have infinite
                         * health enabled.
                         */
                        if (INFINITE_VARS_STORAGE.get(this, InfiniteVarsType.HP) == 0) {
                            setCurrentHp(hp - hitmark.damage)
                        }
                        /*
                         * If the pawn has less than or equal to 0 health,
                         * terminate all queues and begin the death logic.
                         */
                        if (getCurrentHp() <= 0) {
                            hit.actions.forEach { action -> action(hit) }
                            if (entityType.isPlayer) {
                                executePlugin(PlayerDeathAction.deathPlugin)
                            } else {
                                executePlugin(NpcDeathAction.deathPlugin)
                            }
                            hitIterator.remove()
                            break@iterator
                        }
                    }
                    hit.actions.forEach { action -> action(hit) }
                }
                hitIterator.remove()
            }
        }
        if (isDead() && pendingHits.isNotEmpty()) {
            pendingHits.clear()
        }
    }

    /**
     * Handle the [futureRoute] if necessary.
     */
    fun handleFutureRoute() {
        if (futureRoute?.completed == true && futureRoute?.strategy?.cancel == false) {
            val futureRoute = futureRoute!!
            walkPath(futureRoute.route.path, futureRoute.stepType, futureRoute.detectCollision)
            this.futureRoute = null
        }
    }

    /**
     * Walk to all the tiles specified in our [path] queue, using [stepType] as
     * the [MovementQueue.StepType].
     */
    fun walkPath(path: Queue<RSTile>, stepType: MovementQueue.StepType, detectCollision: Boolean) {
        if (path.isEmpty()) {
            if (this is RSPlayer) {
                write(SetMapFlagMessage(255, 255))
            }
            return
        }

        if (timers.has(FROZEN_TIMER)) {
            if (this is RSPlayer) {
                writeMessage(MAGIC_STOPS_YOU_FROM_MOVING)
            }
            return
        }

        if (timers.has(STUN_TIMER)) {
            return
        }

        movementQueue.clear()

        var tail: RSTile? = null
        var next = path.poll()
        while (next != null) {
            movementQueue.addStep(next, stepType, detectCollision)
            val poll = path.poll()
            if (poll == null) {
                tail = next
            }
            next = poll
        }

        /*
         * If the tail is null (should never be unless we mess with code above), or
         * if the tail is the tile we're standing on, then we don't have to move at all!
         */
        if (tail == null || tail.sameAs(tile)) {
            if (this is RSPlayer) {
                write(SetMapFlagMessage(255, 255))
            }
            movementQueue.clear()
            return
        }

        if (this is RSPlayer && lastKnownRegionBase != null) {
            write(SetMapFlagMessage(tail.x - lastKnownRegionBase!!.x, tail.z - lastKnownRegionBase!!.z))
        }
    }

    fun walkTo(tile: RSTile, stepType: MovementQueue.StepType = MovementQueue.StepType.NORMAL, detectCollision: Boolean = true) = walkTo(tile.x, tile.z, stepType, detectCollision)

    fun walkTo(x: Int, z: Int, stepType: MovementQueue.StepType = MovementQueue.StepType.NORMAL, detectCollision: Boolean = true) {
        /*
         * Already standing on requested destination.
         */
        if (tile.x == x && tile.z == z) {
            return
        }

        if (timers.has(FROZEN_TIMER)) {
            if (this is RSPlayer) {
                writeMessage(MAGIC_STOPS_YOU_FROM_MOVING)
            }
            return
        }

        if (timers.has(STUN_TIMER)) {
            return
        }

        val multiThread = world.multiThreadPathFinding
        val request = PathRequest.createWalkRequest(this, x, z, projectile = false, detectCollision = detectCollision)
        val strategy = createPathFindingStrategy(copyChunks = multiThread)

        /*
         * When using multi-thread path-finding, the [PathRequest.createWalkRequest]
         * must have the [tile] in sync with the game-thread, so we need to make sure
         * that in this cycle, the pawn's [tile] does not change. The easiest way to
         * do this is by clearing their movement queue. Though it can cause weird
         */
        if (multiThread) {
            movementQueue.clear()
        }
        futureRoute?.strategy?.cancel = true

        if (multiThread) {
            futureRoute = FutureRoute.of(strategy, request, stepType, detectCollision)
        } else {
            val route = strategy.calculateRoute(request)
            walkPath(route.path, stepType, detectCollision)
        }
    }

    suspend fun walkTo(it: QueueTask, tile: RSTile, stepType: MovementQueue.StepType = MovementQueue.StepType.NORMAL, detectCollision: Boolean = true) = walkTo(it, tile.x, tile.z, stepType, detectCollision)

    suspend fun walkTo(it: QueueTask, x: Int, z: Int, stepType: MovementQueue.StepType = MovementQueue.StepType.NORMAL, detectCollision: Boolean = true): Route {
        /*
         * Already standing on requested destination.
         */
        if (tile.x == x && tile.z == z) {
            return Route(EMPTY_TILE_DEQUE, success = true, tail = RSTile(tile as RSTile))
        }
        val multiThread = world.multiThreadPathFinding
        val request = PathRequest.createWalkRequest(this, x, z, projectile = false, detectCollision = detectCollision)
        val strategy = createPathFindingStrategy(copyChunks = multiThread)

        movementQueue.clear()
        futureRoute?.strategy?.cancel = true

        if (multiThread) {
            futureRoute = FutureRoute.of(strategy, request, stepType, detectCollision)
            while (!futureRoute!!.completed) {
                it.wait(1)
            }
            return futureRoute!!.route
        }

        val route = strategy.calculateRoute(request)
        walkPath(route.path, stepType, detectCollision)
        return route
    }

    fun moveTo(x: Int, z: Int, height: Int = 0) {
        moved = true
        blockBuffer.teleport = !tile.isWithinRadius(x, z, height, RSPlayer.NORMAL_VIEW_DISTANCE)
        tile = RSTile(x, z, height)
        movementQueue.clear()
        addBlock(UpdateBlockType.MOVEMENT)
    }

    fun moveTo(tile: RSTile) {
        moveTo(tile.x, tile.z, tile.height)
    }

    fun animate(id: Int, delay: Int = 0) {
        blockBuffer.animation = id
        blockBuffer.animationDelay = delay
        addBlock(UpdateBlockType.ANIMATION)
    }

    fun graphic(id: Int, height: Int = 0, delay: Int = 0) {
        blockBuffer.graphicId = id
        blockBuffer.graphicHeight = height
        blockBuffer.graphicDelay = delay
        addBlock(UpdateBlockType.GFX)
    }

    fun graphic(graphic: Graphic) {
        graphic(graphic.id, graphic.height, graphic.delay)
    }

    fun forceChat(message: String) {
        blockBuffer.forceChat = message
        addBlock(UpdateBlockType.FORCE_CHAT)
    }

    fun faceTile(face: RSTile, width: Int = 1, length: Int = 1) {
        if (entityType.isPlayer) {
            val srcX = tile.x * 64
            val srcZ = tile.z * 64
            val dstX = face.x * 64
            val dstZ = face.z * 64

            var degreesX = (srcX - dstX).toDouble()
            var degreesZ = (srcZ - dstZ).toDouble()

            degreesX += (Math.floor(width / 2.0)) * 32
            degreesZ += (Math.floor(length / 2.0)) * 32

            blockBuffer.faceDegrees = (Math.atan2(degreesX, degreesZ) * 325.949).toInt() and 0x7ff
        } else if (entityType.isNpc) {
            val faceX = (face.x shl 1) + 1
            val faceZ = (face.z shl 1) + 1
            blockBuffer.faceDegrees = (faceX shl 16) or faceZ
        }

        blockBuffer.facePawnIndex = -1
        addBlock(UpdateBlockType.FACE_TILE)
    }

    fun facePawn(pawn: RSPawn) {
        blockBuffer.faceDegrees = 0

        val index = if (pawn.entityType.isPlayer) pawn.index + 32768 else pawn.index
        if (blockBuffer.facePawnIndex != index) {
            blockBuffer.faceDegrees = 0
            blockBuffer.facePawnIndex = index
            addBlock(UpdateBlockType.FACE_PAWN)
        }

        attr[FACING_PAWN_ATTR] = WeakReference(pawn as Pawn)
    }

    fun resetFacePawn() {
        blockBuffer.faceDegrees = 0

        val index = -1
        if (blockBuffer.facePawnIndex != index) {
            blockBuffer.faceDegrees = 0
            blockBuffer.facePawnIndex = index
            addBlock(UpdateBlockType.FACE_PAWN)
        }

        attr.remove(FACING_PAWN_ATTR)
    }

    /**
     * Resets any interaction this pawn had with another pawn.
     */
    fun resetInteractions() {
        attr.remove(COMBAT_TARGET_FOCUS_ATTR)
        attr.remove(INTERACTING_NPC_ATTR)
        attr.remove(INTERACTING_PLAYER_ATTR)
        resetFacePawn()
    }

    fun queue(priority: TaskPriority = TaskPriority.STANDARD, logic: suspend QueueTask.(CoroutineScope) -> Unit) {
        queues.queue(this, world.coroutineDispatcher, priority, logic)
    }

    /**
     * Terminates any on-going [QueueTask]s that are being executed by this [RSPawn].
     */
    fun interruptQueues() {
        queues.terminateTasks()
    }

    /**
     * Executes a oldplugin with this [RSPawn] as its context.
     */
    fun <T> executePlugin(logic: Plugin.() -> T): T {
        val plugin = Plugin(this)
        return logic(plugin)
    }

    fun triggerEvent(event: Event) {
        world.plugins.executeEvent(this, event)
        world.getService(LoggerService::class.java, searchSubclasses = true)?.logEvent(this, event)
    }

    internal fun createPathFindingStrategy(copyChunks: Boolean = false): PathFindingStrategy {
        val collision: CollisionManager = if (copyChunks) {
            val chunks = world.chunks.copyChunksWithinRadius((tile as RSTile).chunkCoords, height = tile.height, radius = Chunk.CHUNK_VIEW_RADIUS)
            CollisionManager(chunks, createChunksIfNeeded = false)
        } else {
            world.collision
        }
        return if (entityType.isPlayer) BFSPathFindingStrategy(collision) else NpcPathFindingStrategy(collision)
    }

    override fun getAttributes(): AttributeMap {
        return attr
    }

    companion object {
        private val EMPTY_TILE_DEQUE = ArrayDeque<RSTile>()
    }
}
