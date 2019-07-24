package io.rsbox.engine.model

import com.google.common.base.Stopwatch
import io.rsbox.api.World
import io.rsbox.engine.DevContext
import io.rsbox.engine.GameContext
import io.rsbox.engine.RSServer
import io.rsbox.engine.fs.DefinitionSet
import io.rsbox.engine.fs.def.ItemDef
import io.rsbox.engine.fs.def.NpcDef
import io.rsbox.engine.fs.def.ObjectDef
import io.rsbox.engine.message.impl.LogoutFullMessage
import io.rsbox.engine.message.impl.UpdateRebootTimerMessage
import io.rsbox.api.AttributeMap
import io.rsbox.engine.model.collision.CollisionManager
import io.rsbox.engine.model.combat.NpcCombatDef
import io.rsbox.engine.model.entity.*
import io.rsbox.engine.model.instance.InstancedMapAllocator
import io.rsbox.engine.model.npcdrops.NpcDropTableDef
import io.rsbox.engine.model.priv.PrivilegeSet
import io.rsbox.engine.model.queue.QueueTaskSet
import io.rsbox.api.TaskPriority
import io.rsbox.engine.model.queue.RSQueueTask
import io.rsbox.engine.model.queue.impl.WorldQueueTaskSet
import io.rsbox.engine.model.region.ChunkSet
import io.rsbox.engine.model.shop.RSShop
import io.rsbox.engine.model.timer.TimerMap
import io.rsbox.engine.oldplugin.Plugin
import io.rsbox.engine.oldplugin.PluginRepository
import io.rsbox.engine.service.GameService
import io.rsbox.engine.service.Service
import io.rsbox.engine.service.xtea.XteaKeyService
import io.rsbox.engine.sync.block.UpdateBlockSet
import io.rsbox.util.HuffmanCodec
import io.rsbox.util.ServerProperties
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import mu.KLogging
import net.runelite.cache.IndexType
import net.runelite.cache.fs.Store
import java.io.File
import java.security.SecureRandom
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.Random
import java.util.concurrent.TimeUnit

/**
 * The game world, which stores all the entities and nodes that the world
 * needs to keep track of.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class RSWorld(val gameContext: GameContext, val devContext: DevContext) : World {

    /**
     * The [Store] is responsible for handling the data in our cache.
     */
    lateinit var filestore: Store

    /**
     * The [DefinitionSet] that holds general filestore data.
     */
    val definitions = DefinitionSet()

    /**
     * The [HuffmanCodec] used to compress and decompress public chat messages.
     */
    val huffman by lazy {
        val binary = filestore.getIndex(IndexType.BINARY)!!
        val archive = binary.findArchiveByName("huffman")!!
        val file = archive.getFiles(filestore.storage.loadArchive(archive)!!).files[0]
        HuffmanCodec(file.contents)
    }

    /**
     * A collection of our [Service]s specified in our game [ServerProperties]
     * files.
     */
    internal val services = mutableListOf<Service>()

    lateinit var coroutineDispatcher: CoroutineDispatcher

    internal var queues: QueueTaskSet = WorldQueueTaskSet()

    val players = PawnList(arrayOfNulls<RSPlayer>(gameContext.playerLimit))

    val npcs = PawnList(arrayOfNulls<RSNpc>(Short.MAX_VALUE.toInt()))

    val chunks = ChunkSet(this)

    val collision = CollisionManager(chunks)

    val instanceAllocator = InstancedMapAllocator()

    /**
     * The oldplugin repository that's responsible for storing all the plugins found.
     */
    var plugins = PluginRepository(this)

    /**
     * The [PrivilegeSet] that is attached to our game.
     */
    val privileges = PrivilegeSet()

    /**
     * A cached value for [io.rsbox.engine.service.xtea.XteaKeyService] since it
     * is used frequently and in performance critical code. This value is set
     * when [XteaKeyService.init] is called.
     */
    var xteaKeyService: XteaKeyService? = null

    /**
     * The [UpdateBlockSet] for players.
     */
    internal val playerUpdateBlocks = UpdateBlockSet()

    /**
     * The [UpdateBlockSet] for npcs.
     */
    internal val npcUpdateBlocks = UpdateBlockSet()

    /**
     * A [Random] implementation used for pseudo-random purposes through-out
     * the game world.
     */
    val random: Random = SecureRandom()

    /**
     * The amount of game cycles that have gone by since the world was first
     * initialized. This can reset back to [0], if it's signalled to overflow
     * any time soon.
     */
    var currentCycle = 0

    /**
     * Multi-threaded path-finding should be reserved for when the average cycle
     * time is 1-2ms+. This is due to the nature of how the system and game cycles
     * work.
     *
     * Explanation:
     * The path-finder thread tries to calculate the path when the [RSPawn.walkTo]
     * method is called, this happens on the following tasks:
     *
     * Plugin handler: a piece of content needs the player to walk somewhere
     * Message handler: the player's client is requesting to move
     *
     * The [io.rsbox.engine.model.path.FutureRoute.completed] flag is checked on
     * the player pre-synchronization task, right before [MovementQueue.cycle]
     * is called. If the future route is complete, the path is added to the
     * player's movement queue and data is then sent to clients on the player
     * synchronization task.
     *
     * Due to this design, it is likely that the [io.rsbox.engine.model.path.FutureRoute]
     * will not finish calculating the path if the time in between the [RSPawn.walkTo]
     * being called and player pre-synchronization task being executed is fast enough
     *
     * From anecdotal experience, once the average cycle time reaches about 1-2ms+,
     * the multi-threaded path-finding becomes more responsive. However, if the
     * average cycle time is <= 0ms, the path-finder can take one cycle (usually)
     * to complete; this is not because the code is unoptimized - it is because
     * the future route has a window of "total cycle time taken" per cycle
     * to complete.
     *
     * Say a cycle took 250,000 nanoseconds to complete. This means the player
     * pre-synchronization task has already been executed within that time frame.
     * This being the case, the server already checked to see if the future route
     * has completed in those 250,000 nanoseconds. Though the path-finder isn't
     * slow, it's certainly not that fast. So now, the server has to wait until
     * next tick to check if the future route was successful (usually the case,
     * since a whole 600ms have now gone by).
     */
    internal var multiThreadPathFinding = false

    /**
     * RSWorld timers.
     *
     * @see TimerMap
     */
    val timers = TimerMap()

    /**
     * RSWorld attributes.
     *
     * @see AttributeMap
     */
    val attr = AttributeMap()

    /**
     * A local collection of [RSGroundItem]s that are currently spawned. We do
     * not use [ChunkSet]s to iterate through this as it takes quite a bit of
     * time to do so every cycle.
     */
    private val groundItems = ObjectArrayList<RSGroundItem>()

    /**
     * Any ground item that should be spawned in the future. For example, when
     * a 'permanent' ground item is despawned, it will be added here to be spawned
     * after a set amount of cycles.
     */
    private val groundItemQueue = ObjectArrayList<RSGroundItem>()

    /**
     * The amount of time before a server reboot takes place, in game cycles.
     */
    var rebootTimer = -1

    internal fun init() {
        getService(GameService::class.java)?.let { service ->
            coroutineDispatcher = service.dispatcher
        }
    }

    /**
     * Executed after the server has initialised everything, but is not yet bound
     * to a network port.
     */
    internal fun postLoad() {
        plugins.executeWorldInit(this)
    }

    /**
     * Executed every game cycle.
     */
    internal fun cycle() {
        if (currentCycle++ >= Int.MAX_VALUE - 1) {
            currentCycle = 0
            logger.info("RSWorld cycle has been reset.")
        }

        /*
         * Copy the timers to a mutable map just in case a timer has to modify
         * the [timers] during its execution, which isn't uncommon.
         */
        val timersCopy = timers.getTimers().toMutableMap()
        timersCopy.forEach { key, time ->
            if (time <= 0) {
                plugins.executeWorldTimer(this, key)
                if (!timers.has(key)) {
                    timers.remove(key)
                }
            }
        }

        /*
         * Tick all timers down by one cycle.
         */
        timers.getTimers().entries.forEach { timer -> timer.setValue(timer.value - 1) }

        /*
         * Cycle through ground items to handle any despawn or respawn.
         */

        /*
         * Any ground item that should be removed this cycle will be added here.
         */
        val groundItemRemoval = ObjectOpenHashSet<RSGroundItem>(0)

        /*
         * Iterate through our registered [groundItems] and increment their current
         * cycle.
         */
        val groundItemIterator = groundItems.iterator()
        while (groundItemIterator.hasNext()) {
            val groundItem = groundItemIterator.next()

            groundItem.currentCycle++

            if (groundItem.isPublic() && groundItem.currentCycle >= gameContext.gItemDespawnDelay) {
                /*
                 * If the ground item is public and its cycle count has reached the
                 * despawn delay set by our game, we add it to our removal queue.
                 */
                groundItemRemoval.add(groundItem)
            } else if (!groundItem.isPublic() && groundItem.currentCycle >= gameContext.gItemPublicDelay) {
                /*
                 * If the ground item is not public, but its cycle count has
                 * reached the public delay set by our game, we make it public.
                 */
                groundItem.removeOwner()
                chunks.get(groundItem.tile as RSTile)?.let { chunk ->
                    chunk.removeEntity(this, groundItem, groundItem.tile as RSTile)
                    chunk.addEntity(this, groundItem, groundItem.tile as RSTile)
                }
            }
        }

        /*
         * We now remove any ground item that was queued for removal.
         * We also check to see if they should respawn after a set amount
         * of cycles; if so, we append it to our [groundItemQueue] to be
         * spawned at a later point in time.
         */
        groundItemRemoval.forEach { item ->
            remove(item)
            if (item.respawnCycles > 0) {
                item.currentCycle = 0
                groundItemQueue.add(item)
            }
        }

        /*
         * Go over our [groundItemQueue] and respawn any ground item that has
         * met the respawn criteria.
         */
        val groundItemQueueIterator = groundItemQueue.iterator()
        while (groundItemQueueIterator.hasNext()) {
            val item = groundItemQueueIterator.next()
            item.currentCycle++
            if (item.currentCycle >= item.respawnCycles) {
                item.currentCycle = 0
                spawn(item)
                groundItemQueueIterator.remove()
            }
        }

        /*
         * Cycle through shops for their resupply ticks.
         */
        plugins.shops.values.forEach { it.cycle(this) }

        /*
         * Cycle through instanced maps.
         */
        instanceAllocator.cycle(this)

        if (rebootTimer > 0) {
            rebootTimer--

            if (rebootTimer == 0) {
                for (i in 0 until players.capacity) {
                    players[i]?.let { player ->
                        player.handleLogout()
                        player.write(LogoutFullMessage())
                        player.channelClose()
                    }
                }
            }
        }
    }

    /**
     * Sends the reboot timer to all registered players.
     */
    fun sendRebootTimer(cycles: Int = rebootTimer) {
        players.forEach { p ->
            p.write(UpdateRebootTimerMessage(cycles))
        }
    }

    fun register(p: RSPlayer): Boolean {
        val registered = players.add(p)
        if (registered) {
            p.lastIndex = p.index
            return true
        }
        return false
    }

    fun unregister(p: RSPlayer) {
        players.remove(p)
        chunks.get(p.tile as RSTile)?.removeEntity(this, p, p.tile as RSTile)
    }

    fun spawn(npc: RSNpc): Boolean {
        val added = npcs.add(npc)
        if (added) {
            setNpcDefaults(npc)
            plugins.executeNpcSpawn(npc)
        }
        return added
    }

    fun remove(npc: RSNpc) {
        npcs.remove(npc)
        chunks.get(npc.tile as RSTile)?.removeEntity(this, npc, npc.tile as RSTile)
    }

    fun spawn(obj: RSGameObject) {
        val tile = obj.tile
        val chunk = chunks.getOrCreate(tile as RSTile)

        val oldObj = chunk.getEntities<RSGameObject>(tile, EntityType.STATIC_OBJECT, EntityType.DYNAMIC_OBJECT).firstOrNull { it.type == obj.type }
        if (oldObj != null) {
            chunk.removeEntity(this, oldObj, tile)
        }
        chunk.addEntity(this, obj, tile)
    }

    fun remove(obj: RSGameObject) {
        val tile = obj.tile
        val chunk = chunks.getOrCreate(tile as RSTile)

        chunk.removeEntity(this, obj, tile)
    }

    fun spawn(item: RSGroundItem) {
        val tile = item.tile
        val chunk = chunks.getOrCreate(tile as RSTile)

        val def = definitions.get(ItemDef::class.java, item.item)

        if (def.stackable) {
            val oldItem = chunk.getEntities<RSGroundItem>(tile, EntityType.GROUND_ITEM).firstOrNull { it.item == item.item && it.ownerUID == item.ownerUID }
            if (oldItem != null) {
                val oldAmount = oldItem.amount
                val newAmount = Math.min(Int.MAX_VALUE.toLong(), item.amount.toLong() + oldItem.amount.toLong()).toInt()
                oldItem.amount = newAmount
                chunk.updateGroundItem(this, item, oldAmount, newAmount)
                return
            }
        }

        groundItems.add(item)
        chunk.addEntity(this, item, tile)
    }

    fun remove(item: RSGroundItem) {
        val tile = item.tile
        val chunk = chunks.getOrCreate(tile as RSTile)

        groundItems.remove(item)
        chunk.removeEntity(this, item, tile)

        if (item.respawnCycles > 0) {
            item.currentCycle = 0
            groundItemQueue.add(item)
        }
    }

    fun spawn(projectile: Projectile) {
        val tile = projectile.tile
        val chunk = chunks.getOrCreate(tile as RSTile)

        chunk.addEntity(this, projectile, tile)
    }

    fun spawn(sound: AreaSound) {
        val tile = sound.tile
        val chunk = chunks.getOrCreate(tile as RSTile)

        chunk.addEntity(this, sound, tile)
    }

    /**
     * Despawn entities in an area.
     */
    fun removeAll(area: Area) {
        for (i in 0 until npcs.capacity) {
            val npc = npcs[i] ?: continue
            if (area.contains(npc.tile as RSTile)) {
                remove(npc)
            }
        }

        for (i in 0 until groundItems.size) {
            val item = groundItems[i] ?: continue
            if (area.contains(item.tile as RSTile)) {
                remove(item)
            }
        }
    }

    fun isSpawned(obj: RSGameObject): Boolean = chunks.getOrCreate(obj.tile as RSTile).getEntities<RSGameObject>(obj.tile as RSTile, EntityType.STATIC_OBJECT, EntityType.DYNAMIC_OBJECT).contains(obj)

    fun isSpawned(item: RSGroundItem): Boolean = chunks.getOrCreate(item.tile as RSTile).getEntities<RSGroundItem>(item.tile as RSTile, EntityType.GROUND_ITEM).contains(item)

    /**
     * Get any [RSGroundItem] that matches the [predicate].
     *
     * @return null if no ground item meets the conditions of [predicate].
     */
    fun getGroundItem(predicate: (RSGroundItem) -> Boolean): RSGroundItem? = groundItems.firstOrNull { predicate(it) }

    /**
     * Gets the [RSGameObject] that is located on [tile] and has a
     * [RSGameObject.type] equal to [type].
     *
     * @return
     * null if no [RSGameObject] with [type] was found in [tile].
     */
    fun getObject(tile: RSTile, type: Int): RSGameObject? = chunks.get(tile, createIfNeeded = true)!!.getEntities<RSGameObject>(tile, EntityType.STATIC_OBJECT, EntityType.DYNAMIC_OBJECT).firstOrNull { it.type == type }

    fun getPlayerForName(username: String): RSPlayer? {
        for (i in 0 until players.capacity) {
            val player = players[i] ?: continue
            if (player.username.equals(username, ignoreCase = true)) {
                return player
            }
        }
        return null
    }

    fun getPlayerForUid(uid: PlayerUID): RSPlayer? = players.firstOrNull { it.uid.value == uid.value }

    fun getShop(name: String): RSShop? = plugins.shops.getOrDefault(name, null)

    fun getMultiCombatChunks(): Set<Int> = plugins.multiCombatChunks

    fun getMultiCombatRegions(): Set<Int> = plugins.multiCombatRegions

    fun random(boundInclusive: Int) = random.nextInt(boundInclusive + 1)

    fun random(range: IntRange): Int = random.nextInt(range.endInclusive - range.start + 1) + range.start

    fun randomDouble(): Double = random.nextDouble()

    fun chance(chance: Int, probability: Int): Boolean {
        check(chance in 1..probability) { "Chance must be within range of (0 - probability]" }
        return random.nextInt(probability) <= chance - 1
    }

    fun percentChance(chance: Double): Boolean {
        check(chance in 0.0..100.0) { "Chance must be within range of [0.0 - 100.0]" }
        return random.nextDouble() <= (chance / 100.0)
    }

    fun findRandomTileAround(centre: RSTile, radius: Int, centreWidth: Int = 0, centreLength: Int = 0): RSTile? {
        val tiles = mutableListOf<RSTile>()
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                if (x in 0 until centreWidth && z in 0 until centreLength) {
                    continue
                }
                tiles.add(centre.transform(x, z) as RSTile)
            }
        }
        val filtered = tiles.filter { tile -> !collision.isClipped(tile) }
        if (filtered.isNotEmpty()) {
            return filtered.random()
        }
        return null
    }

    fun queue(logic: suspend RSQueueTask.(CoroutineScope) -> Unit) {
        queues.queue(this, coroutineDispatcher, TaskPriority.STANDARD, logic)
    }

    fun executePlugin(ctx: Any, logic: (Plugin).() -> Unit) {
        val plugin = Plugin(ctx)
        logic(plugin)
    }

    fun sendExamine(p: RSPlayer, id: Int, type: ExamineEntityType) {
        val examine = when (type) {
            ExamineEntityType.ITEM -> definitions.get(ItemDef::class.java, id).examine
            ExamineEntityType.NPC -> definitions.get(NpcDef::class.java, id).examine
            ExamineEntityType.OBJECT -> definitions.get(ObjectDef::class.java, id).examine
        }

        if (examine != null) {
            val extension = if (devContext.debugExamines) " ($id)" else ""
            p.writeMessage(examine + extension)
        } else {
            logger.warn { "No examine info found for entity [$type, $id]" }
        }
    }

    fun setNpcDefaults(npc: RSNpc) {
        val combatDef = plugins.npcCombatDefs.getOrDefault(npc.id, null) ?: NpcCombatDef.DEFAULT
        npc.combatDef = combatDef

        npc.combatDef.bonuses.forEachIndexed { index, bonus -> npc.equipmentBonuses[index] = bonus }
        npc.respawns = combatDef.respawnDelay > 0

        npc.setCurrentHp(npc.combatDef.hitpoints)
        combatDef.stats.forEachIndexed { index, level ->
            npc.stats.setMaxLevel(index, level)
            npc.stats.setCurrentLevel(index, level)
        }

        val dropTableDef = plugins.npcDropTableDefs.getOrDefault(npc.id, null) ?: NpcDropTableDef.DEFAULT
        npc.dropTableDef = dropTableDef
    }

    /**
     * Gets the first service that can be found which meets the criteria of:
     *
     * When [searchSubclasses] is true: the service class must be assignable to the [type].
     * When [searchSubclasses] is false: the service class must be equal to the [type].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Service> getService(type: Class<out T>, searchSubclasses: Boolean = false): T? {
        if (searchSubclasses) {
            return services.firstOrNull { type.isAssignableFrom(it::class.java) } as T?
        }
        return services.firstOrNull { it::class.java == type } as T?
    }

    /**
     * Loads all the services listed on our game properties file.
     */
    internal fun loadServices(server: RSServer, gameProperties: ServerProperties) {
        val stopwatch = Stopwatch.createUnstarted()
        val foundServices = gameProperties.get<ArrayList<Any>>("services")!!
        foundServices.forEach { s ->
            val values = s as LinkedHashMap<*, *>
            val className = values["class"] as String
            val clazz = Class.forName(className).asSubclass(Service::class.java)!!
            val service = clazz.newInstance()

            val properties = hashMapOf<String, Any>()
            values.filterKeys { it != "class" }.forEach { key, value ->
                properties[key as String] = value
            }

            stopwatch.reset().start()
            service.init(server, this, ServerProperties().loadMap(properties))
            stopwatch.stop()

            services.add(service)
            logger.info("Initiated service '{}' in {}ms.", service.javaClass.simpleName, stopwatch.elapsed(TimeUnit.MILLISECONDS))
        }
        services.forEach { s -> s.postLoad(server, this) }
        logger.info("Loaded {} game services.", services.size)
    }

    /**
     * Load the external [UpdateBlockSet] data.
     */
    internal fun loadUpdateBlocks(blocksFile: File) {
        val properties = ServerProperties().loadYaml(blocksFile)

        if (properties.has("players")) {
            playerUpdateBlocks.load(properties.extract("players"))
        }

        if (properties.has("npcs")) {
            npcUpdateBlocks.load(properties.extract("npcs"))
        }
    }

    /**
     * Invoke network related logic for all services.
     */
    internal fun bindServices(server: RSServer) {
        services.forEach { it.bindNet(server, this) }
    }

    companion object : KLogging() {

        /**
         * If the [rebootTimer] is active and is less than this value, we will
         * begin to reject any log-in.
         */
        const val REJECT_LOGIN_REBOOT_THRESHOLD = 50
    }
}