package io.rsbox.engine.model.entity

import com.google.common.base.MoreObjects
import com.google.common.primitives.Ints
import io.rsbox.api.*
import io.rsbox.api.event.EventManager
import io.rsbox.api.event.login.PlayerLoginEvent
import io.rsbox.api.Appearance
import io.rsbox.api.entity.Player
import io.rsbox.engine.fs.def.ItemDef
import io.rsbox.engine.fs.def.VarbitDef
import io.rsbox.engine.fs.def.VarpDef
import io.rsbox.engine.game.DeathContainers
import io.rsbox.engine.game.EquipmentType
import io.rsbox.engine.game.Game
import io.rsbox.engine.game.Skills
import io.rsbox.engine.message.Message
import io.rsbox.engine.message.impl.*
import io.rsbox.engine.model.*
import io.rsbox.engine.model.container.ContainerStackType
import io.rsbox.engine.model.container.ItemContainer
import io.rsbox.engine.model.container.key.*
import io.rsbox.engine.model.interf.*
import io.rsbox.engine.model.interf.listener.PlayerInterfaceListener
import io.rsbox.engine.model.item.RSItem
import io.rsbox.engine.model.priv.Privilege
import io.rsbox.engine.model.queue.RSQueueTask
import io.rsbox.engine.model.shop.RSShop
import io.rsbox.engine.model.skill.SkillSet
import io.rsbox.engine.model.timer.ACTIVE_COMBAT_TIMER
import io.rsbox.engine.model.timer.FORCE_DISCONNECTION_TIMER
import io.rsbox.engine.model.varp.VarpSet
import io.rsbox.engine.service.log.LoggerService
import io.rsbox.api.UpdateBlockType
import io.rsbox.util.BitManipulation
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import java.util.Arrays

/**
 * A [RSPawn] that represents a player.
 *
 * @author Tom <rspsmods@gmail.com>
 */
open class RSPlayer(world: RSWorld) : RSPawn(world), Player {
    /**
     * A persistent and unique id. This is <strong>not</strong> the index
     * of our [RSPlayer] when registered to the [RSWorld], it is a value determined
     * when the [RSPlayer] first registers their account.
     */
    lateinit var uid: PlayerUID

    /**
     * The display name that will show on the player while in-game.
     */
    override var username = ""

    /**
     * @see Privilege
     */
    var privilege = Privilege.DEFAULT

    /**
     * The base region [Coordinate] is the most bottom-left (south-west) tile where
     * the last known region for this player begins.
     */
    var lastKnownRegionBase: Coordinate? = null

    /**
     * A flag that indicates whether or not the [login] method has been executed.
     * This is currently used so that we don't send player updates when the player
     * hasn't been fully initialized. We can test later to see if this is even
     * necessary.
     */
    var initiated = false

    /**
     * The index that was assigned to a [RSPlayer] when they are first registered to the
     * [RSWorld]. This is needed to remove local players from the synchronization task
     * as once that logic is reached, the local player would have an index of [-1].
     */
    var lastIndex = -1

    /**
     * A flag which indicates the player is attempting to log out. There can be
     * certain circumstances where the player should not be unregistered from
     * the world.
     *
     * For example: when the player is in combat.
     */
    @Volatile private var pendingLogout = false

    /**
     * A flag which indicates that our [FORCE_DISCONNECTION_TIMER] must be set
     * when [pendingLogout] logic is handled.
     */
    @Volatile private var setDisconnectionTimer = false

    val inventory = ItemContainer(world.definitions, INVENTORY_KEY)

    val equipment = ItemContainer(world.definitions, EQUIPMENT_KEY)

    val bank = ItemContainer(world.definitions, BANK_KEY)

    /**
     * A map that contains all the [ItemContainer]s a player can have.
     */
    val containers = HashMap<ContainerKey, ItemContainer>().apply {
        put(INVENTORY_KEY, inventory)
        put(EQUIPMENT_KEY, equipment)
        put(BANK_KEY, bank)
    }

    val interfaces by lazy { InterfaceSet(PlayerInterfaceListener(this, world.plugins)) }

    val varps = VarpSet(maxVarps = world.definitions.getCount(VarpDef::class.java))

    private val skillSet = SkillSet(maxSkills = world.gameContext.skillCount)

    var lastRegionId: Int = -1

    /**
     * The options that can be executed on this player
     */
    val options = Array<String?>(10) { null }

    /**
     * Flag that indicates whether or not to refresh the shop the player currently
     * has open.
     */
    var shopDirty = false

    /**
     * Some areas have a 'large' viewport. Which means the player's client is
     * able to render more entities in a larger radius than normal.
     */
    private var largeViewport = false

    /**
     * The players in our viewport, including ourselves. This list should not
     * be used outside of our synchronization task.
     */
    internal val gpiLocalPlayers = arrayOfNulls<RSPlayer>(2048)

    /**
     * The indices of any possible local player in the world.
     */
    internal val gpiLocalIndexes = IntArray(2048)

    /**
     * The current local player count.
     */
    internal var gpiLocalCount = 0

    /**
     * The indices of players outside of our viewport in the world.
     */
    internal val gpiExternalIndexes = IntArray(2048)

    /**
     * The amount of players outside of our viewport.
     */
    internal var gpiExternalCount = 0

    /**
     * The inactivity flags for players.
     */
    internal val gpiInactivityFlags = IntArray(2048)

    /**
     * GPI tile hash multipliers.
     *
     * The player synchronization task will send [RSTile.x] and [RSTile.z] as 13-bit
     * values, which is 2^13 (8192). To send a player position higher than said
     * value in either direction, we must also send a multiplier.
     */
    internal val gpiTileHashMultipliers = IntArray(2048)

    /**
     * The npcs in our viewport. This list should not be used outside of our
     * synchronization task.
     */
    internal val localNpcs = ObjectArrayList<RSNpc>()

    var appearance = RSAppearance.DEFAULT

    var weight = 0.0

    var skullIcon = -1

    var runEnergy = 100.0

    /**
     * The current combat level. This must be set externally by a login oldplugin
     * that is used on whatever revision you want.
     */
    var combatLevel = 3

    var gameMode = 0

    var xpRate = 1.0

    /**
     * The last cycle that this client has received the MAP_BUILD_COMPLETE
     * message. This value is set to [RSWorld.currentCycle].
     *
     * @see [io.rsbox.engine.message.handler.MapBuildCompleteHandler]
     */
    var lastMapBuildTime = 0

    fun getSkills(): SkillSet = skillSet

    override val entityType: EntityType = EntityType.PLAYER

    /**
     * Checks if the player is running. We assume that the varp with id of
     * [173] is the running state varp.
     */
    override fun isRunning(): Boolean = varps[173].state != 0 || movementQueue.peekLastStep()?.type == MovementQueue.StepType.FORCED_RUN

    override fun getSize(): Int = 1

    override fun getCurrentHp(): Int = getSkills().getCurrentLevel(3)

    override fun getMaxHp(): Int = getSkills().getMaxLevel(3)

    override fun setCurrentHp(level: Int) {
        getSkills().setCurrentLevel(3, level)
    }

    override fun addBlock(block: UpdateBlockType) {
        val bits = world.playerUpdateBlocks.updateBlocks[block]!!
        blockBuffer.addBit(bits.bit)
    }

    override fun hasBlock(block: UpdateBlockType): Boolean {
        val bits = world.playerUpdateBlocks.updateBlocks[block]!!
        return blockBuffer.hasBit(bits.bit)
    }

    fun forceMove(movement: ForcedMovement) {
        blockBuffer.forceMovement = movement
        addBlock(UpdateBlockType.FORCE_MOVEMENT)
    }

    suspend fun forceMove(task: RSQueueTask, movement: ForcedMovement, cycleDuration: Int = movement.maxDuration / 30) {
        movementQueue.clear()
        lock = LockState.DELAY_ACTIONS

        lastTile = RSTile(tile as RSTile)
        moveTo(movement.finalDestination)

        forceMove(movement)

        task.wait(cycleDuration)
        lock = LockState.NONE
    }

    /**
     * Logic that should be executed every game cycle, before
     * [io.rsbox.engine.sync.task.PlayerSynchronizationTask].
     *
     * Note that this method may be handled in parallel, so be careful with race
     * conditions if any logic may modify other [RSPawn]s.
     */
    override fun cycle() {
        var calculateWeight = false
        var calculateBonuses = false

        if (pendingLogout) {

            /*
             * If a channel is suddenly inactive (disconnected), we don't to
             * immediately unregister the player. However, we do want to
             * unregister the player abruptly if a certain amount of time
             * passes since their channel disconnected.
             */
            if (setDisconnectionTimer) {
                timers[FORCE_DISCONNECTION_TIMER] = 250 // 2 mins 30 secs
                setDisconnectionTimer = false
            }

            /*
             * A player should only be unregistered from the world when they
             * do not have [ACTIVE_COMBAT_TIMER] or its cycles are <= 0, or if
             * their channel has been inactive for a while.
             *
             * We do allow players to disconnect even if they are in combat, but
             * only if the most recent damage dealt to them are by npcs.
             */
            val stopLogout = timers.has(ACTIVE_COMBAT_TIMER) && damageMap.getAll(type = EntityType.PLAYER, timeFrameMs = 10_000).isNotEmpty()
            val forceLogout = timers.exists(FORCE_DISCONNECTION_TIMER) && !timers.has(FORCE_DISCONNECTION_TIMER)

            if (!stopLogout || forceLogout) {
                if (lock.canLogout()) {
                    handleLogout()
                    return
                }
            }
        }

        val oldRegion = lastRegionId
        if (oldRegion != tile.regionId) {
            if (oldRegion != -1) {
                world.plugins.executeRegionExit(this, oldRegion)
            }
            world.plugins.executeRegionEnter(this, tile.regionId)
        }

        lastRegionId = tile.regionId

        if (inventory.dirty) {
            write(UpdateInvFullMessage(interfaceId = 149, component = 0, containerKey = 93, items = inventory.rawItems))
            inventory.dirty = false
            calculateWeight = true
        }

        if (equipment.dirty) {
            write(UpdateInvFullMessage(containerKey = 94, items = equipment.rawItems))
            equipment.dirty = false
            calculateWeight = true
            calculateBonuses = true

            addBlock(UpdateBlockType.APPEARANCE)
        }

        if (bank.dirty) {
            write(UpdateInvFullMessage(containerKey = 95, items = bank.rawItems))
            bank.dirty = false
        }

        if (shopDirty) {
            attr[CURRENT_SHOP_ATTR]?.let { shop ->
                write(UpdateInvFullMessage(containerKey = 13, items = (shop as RSShop).items.map { if (it != null) RSItem(it.item, it.currentAmount) else null }.toTypedArray()))
            }
            shopDirty = false
        }

        if (calculateWeight) {
            calculateWeight()
        }

        if (calculateBonuses) {
            calculateBonuses()
        }

        if (timers.isNotEmpty) {
            timerCycle()
        }

        hitsCycle()

        for (i in 0 until varps.maxVarps) {
            if (varps.isDirty(i)) {
                val varp = varps[i]
                val message = when {
                    varp.state in -Byte.MAX_VALUE..Byte.MAX_VALUE -> VarpSmallMessage(varp.id, varp.state)
                    else -> VarpLargeMessage(varp.id, varp.state)
                }
                write(message)
            }
        }
        varps.clean()

        for (i in 0 until getSkills().maxSkills) {
            if (getSkills().isDirty(i)) {
                write(UpdateStatMessage(skill = i, level = getSkills().getCurrentLevel(i), xp = getSkills().getCurrentXp(i).toInt()))
                getSkills().clean(i)
            }
        }
    }

    /**
     * Logic that should be executed every game cycle, after
     * [io.rsbox.engine.sync.task.PlayerSynchronizationTask].
     *
     * Note that this method may be handled in parallel, so be careful with race
     * conditions if any logic may modify other [RSPawn]s.
     */
    fun postCycle() {
        /*
         * Flush the channel at the end.
         */
        channelFlush()
    }

    /**
     * Registers this player to the [world].
     */
    fun register(): Boolean = world.register(this)

    /**
     * Handles any logic that should be executed upon log in.
     */
    fun login() {
        if (entityType.isHumanControlled) {
            gpiLocalPlayers[index] = this
            gpiLocalIndexes[gpiLocalCount++] = index

            for (i in 1 until 2048) {
                if (i == index) {
                    continue
                }
                gpiExternalIndexes[gpiExternalCount++] = i
                gpiTileHashMultipliers[i] = if (i < world.players.capacity) world.players[i]?.tile?.asTileHashMultiplier ?: 0 else 0
            }

            val tiles = IntArray(gpiTileHashMultipliers.size)
            System.arraycopy(gpiTileHashMultipliers, 0, tiles, 0, tiles.size)

            write(RebuildLoginMessage(index, tile as RSTile, tiles, world.xteaKeyService))
            world.getService(LoggerService::class.java, searchSubclasses = true)?.logLogin(this)
        }

        if (world.rebootTimer != -1) {
            write(UpdateRebootTimerMessage(world.rebootTimer))
        }

        initiated = true
        Game.login(this)

        if(EventManager.fireEvent(PlayerLoginEvent(player = this as Player, world = this.world as World))) {
            io.rsbox.engine.game.events.PlayerLoginEvent.execute(this)
        }
    }

    /**
     * Requests for this player to log out. However, the player may not be able
     * to log out immediately under certain circumstances.
     */
    fun requestLogout() {
        pendingLogout = true
        setDisconnectionTimer = true
    }

    /**
     * Handles the logic that must be executed once a player has successfully
     * logged out. This means all the prerequisites have been met for the player
     * to log out of the [world].
     *
     * The [RSClient] implementation overrides this method and will handle saving
     * data for the player and call this super method at the end.
     */
    internal open fun handleLogout() {
        interruptQueues()
        world.instanceAllocator.logout(this)
        world.plugins.executeLogout(this)
        world.unregister(this)
    }

    fun calculateWeight() {
        val inventoryWeight = inventory.filterNotNull().sumByDouble { it.getDef(world.definitions).weight }
        val equipmentWeight = equipment.filterNotNull().sumByDouble { it.getDef(world.definitions).weight }
        this.weight = inventoryWeight + equipmentWeight
        write(UpdateRunWeightMessage(this.weight.toInt()))
    }

    fun calculateBonuses() {
        Arrays.fill(equipmentBonuses, 0)
        for (i in 0 until equipment.capacity) {
            val item = equipment[i] ?: continue
            val def = item.getDef(world.definitions)
            def.bonuses.forEachIndexed { index, bonus -> equipmentBonuses[index] += bonus }
        }
    }

    fun addXp(skill: Int, xp: Double) {
        val oldXp = getSkills().getCurrentXp(skill)
        if (oldXp >= SkillSet.MAX_XP) {
            return
        }
        val newXp = Math.min(SkillSet.MAX_XP.toDouble(), (oldXp + (xp * xpRate)))
        /*
         * Amount of levels that have increased with the addition of [xp].
         */
        val increment = SkillSet.getLevelForXp(newXp) - SkillSet.getLevelForXp(oldXp)

        /*
         * Only increment the 'current' level if it's set at its capped level.
         */
        if (getSkills().getCurrentLevel(skill) == getSkills().getMaxLevel(skill)) {
            getSkills().setBaseXp(skill, newXp)
        } else {
            getSkills().setXp(skill, newXp)
        }

        if (increment > 0) {
            attr[LEVEL_UP_SKILL_ID] = skill
            attr[LEVEL_UP_INCREMENT] = increment
            attr[LEVEL_UP_OLD_XP] = oldXp
            world.plugins.executeSkillLevelUp(this)
        }
    }

    override fun getInterfaceAt(dest: InterfaceDestination): Int {
        val displayMode = interfaces.displayMode
        val child = getChildId(dest, displayMode)
        val parent = getDisplayComponentId(displayMode)
        return interfaces.getInterfaceAt(parent, child)
    }

    override fun openOverlayInterface(displayMode: DisplayMode) {
        if (displayMode != interfaces.displayMode) {
            interfaces.setVisible(parent = getDisplayComponentId(interfaces.displayMode), child = getChildId(
                InterfaceDestination.MAIN_SCREEN,
                interfaces.displayMode
            ), visible = false)
        }
        val component = getDisplayComponentId(displayMode)
        interfaces.setVisible(parent = getDisplayComponentId(displayMode), child = 0, visible = true)
        write(IfOpenTopMessage(component))
    }

    override fun openInterface(dest: InterfaceDestination, autoClose: Boolean) {
        val displayMode = if (!autoClose || dest.fullscreenChildId == -1) interfaces.displayMode else DisplayMode.FULLSCREEN
        val child = getChildId(dest, displayMode)
        val parent = getDisplayComponentId(displayMode)
        if (displayMode == DisplayMode.FULLSCREEN) {
            openOverlayInterface(displayMode)
        }
        openInterface(parent, child, dest.interfaceId, if (dest.clickThrough) 1 else 0, isModal = dest == InterfaceDestination.MAIN_SCREEN)
    }

    override fun openInterface(parent: Int, child: Int, interfaceId: Int, type: Int, isModal: Boolean) {
        if (isModal) {
            interfaces.openModal(parent, child, interfaceId)
        } else {
            interfaces.open(parent, child, interfaceId)
        }
        write(IfOpenSubMessage(parent, child, interfaceId, type))
    }

    override fun openInterface(interfaceId: Int, dest: InterfaceDestination, fullscreen: Boolean) {
        val displayMode = if (!fullscreen || dest.fullscreenChildId == -1) interfaces.displayMode else DisplayMode.FULLSCREEN
        val child = getChildId(dest, displayMode)
        val parent = getDisplayComponentId(displayMode)
        if (displayMode == DisplayMode.FULLSCREEN) {
            openOverlayInterface(displayMode)
        }
        openInterface(parent, child, interfaceId, if (dest.clickThrough) 1 else 0, isModal = dest == InterfaceDestination.MAIN_SCREEN)
    }

    override fun closeInterface(interfaceId: Int) {
        if (interfaceId == interfaces.getModal()) {
            interfaces.setModal(-1)
        }
        val hash = interfaces.close(interfaceId)
        if (hash != -1) {
            write(IfCloseSubMessage(hash))
        }
    }

    override fun closeInterface(dest: InterfaceDestination) {
        val displayMode = interfaces.displayMode
        val child = getChildId(dest, displayMode)
        val parent = getDisplayComponentId(displayMode)
        val hash = interfaces.close(parent, child)
        if (hash != -1) {
            write(IfCloseSubMessage((parent shl 16) or child))
        }
    }

    override fun closeComponent(parent: Int, child: Int) {
        interfaces.close(parent, child)
        write(IfCloseSubMessage((parent shl 16) or child))
    }

    fun closeInputDialog() {
        write(TriggerOnDialogAbortMessage())
    }

    fun isInterfaceVisible(interfaceId: Int): Boolean = interfaces.isVisible(interfaceId)

    fun toggleDisplayInterface(newMode: DisplayMode) {
        if (interfaces.displayMode != newMode) {
            val oldMode = interfaces.displayMode
            interfaces.displayMode = newMode

            openOverlayInterface(newMode)

            InterfaceDestination.values.filter { it.isSwitchable() }.forEach { pane ->
                val fromParent = getDisplayComponentId(oldMode)
                val fromChild = getChildId(pane, oldMode)
                val toParent = getDisplayComponentId(newMode)
                val toChild = getChildId(pane, newMode)

                /*
                 * Remove the interfaces from the old display mode's chilren and add
                 * them to the new display mode's children.
                 */
                if (interfaces.isOccupied(parent = fromParent, child = fromChild)) {
                    val oldComponent = interfaces.close(parent = fromParent, child = fromChild)
                    if (oldComponent != -1) {
                        if (pane != InterfaceDestination.MAIN_SCREEN) {
                            interfaces.open(parent = toParent, child = toChild, interfaceId = oldComponent)
                        } else {
                            interfaces.openModal(parent = toParent, child = toChild, interfaceId = oldComponent)
                        }
                    }
                }

                write(IfMoveSubMessage(from = (fromParent shl 16) or fromChild, to = (toParent shl 16) or toChild))
            }

            if (newMode.isResizable()) {
                setInterfaceUnderlay(color = -1, transparency = -1)
            }
            if (oldMode.isResizable()) {
                openInterface(parent = getDisplayComponentId(newMode), child = getChildId(
                    InterfaceDestination.MAIN_SCREEN,
                    newMode
                ), interfaceId = 60, type = 0)
            }
        }
    }

    override fun setInterfaceUnderlay(color: Int, transparency: Int) {
        runClientScript(2524, color, transparency)
    }

    fun calculateAndSetCombatLevel(): Boolean {
        val old = combatLevel

        val attack = getSkills().getMaxLevel(Skills.ATTACK)
        val defence = getSkills().getMaxLevel(Skills.DEFENCE)
        val strength = getSkills().getMaxLevel(Skills.STRENGTH)
        val hitpoints = getSkills().getMaxLevel(Skills.HITPOINTS)
        val prayer = getSkills().getMaxLevel(Skills.PRAYER)
        val ranged = getSkills().getMaxLevel(Skills.RANGED)
        val magic = getSkills().getMaxLevel(Skills.MAGIC)

        val base = Ints.max(strength + attack, magic * 2, ranged * 2)

        combatLevel = ((base * 1.3 + defence + hitpoints + prayer / 2) / 4).toInt()

        val changed = combatLevel != old
        if (changed) {
            runClientScript(389, combatLevel)

            setComponentText(593, 2, "Combat Lvl: $combatLevel")

            addBlock(UpdateBlockType.APPEARANCE)
            return true
        }

        return false
    }

    fun calculateDeathContainers(): DeathContainers {
        var keepAmount = 3
        if (attr[PROTECT_ITEM_ATTR] == true) {
            keepAmount++
        }

        val keptContainer = ItemContainer(world.definitions, keepAmount, ContainerStackType.NO_STACK)
        val lostContainer = ItemContainer(world.definitions, inventory.capacity + equipment.capacity, ContainerStackType.NORMAL)

        var totalItems = inventory.rawItems.filterNotNull() + equipment.rawItems.filterNotNull()
        val valueService = null

        @Suppress("SENSELESS_COMPARISON")
        totalItems = if (valueService != null) {
            totalItems.sortedBy { it.id }.sortedWith(compareByDescending { it.id })
        } else {
            totalItems.sortedBy { it.id }.sortedWith(compareByDescending { world.definitions.get(ItemDef::class.java, it.id).cost })
        }

        totalItems.forEach { item ->
            if (keepAmount > 0 && !keptContainer.isFull) {
                val add = keptContainer.add(item, assureFullInsertion = false)
                keepAmount -= add.completed
                if (add.getLeftOver() > 0) {
                    lostContainer.add(item.id, add.getLeftOver())
                }
            } else {
                lostContainer.add(item)
            }
        }

        return DeathContainers(kept = keptContainer, lost = lostContainer)
    }

    fun sendWeaponComponentInformation() {
        val weapon = getEquipment(EquipmentType.WEAPON)

        val name: String
        val panel: Int

        if (weapon != null) {
            val definition = world.definitions.get(ItemDef::class.java, weapon.id)
            name = definition.name

            panel = Math.max(0, definition.weaponType)
        } else {
            name = "Unarmed"
            panel = 0
        }

        setComponentText(593, 1, name)
        setVarbit(357, panel)
    }

    fun getEquipment(slot: EquipmentType): RSItem? = equipment[slot.id]

    /**
     * @see largeViewport
     */
    fun setLargeViewport(largeViewport: Boolean) {
        this.largeViewport = largeViewport
    }

    /**
     * @see largeViewport
     */
    fun hasLargeViewport(): Boolean = largeViewport

    /**
     * Invoked when the player should close their current interface modal.
     */
    internal fun closeInterfaceModal() {
        Game.closeModal(this)
    }

    /**
     * Checks if the player is registered to a [PawnList] as they should be
     * solely responsible for write access on the index. Being registered
     * to the list should essentially mean the player is registered to the
     * [world].
     *
     * @return
     * true if the player is registered to a [PawnList].
     */
    val isOnline: Boolean get() = index > 0

    /**
     * Default method to handle any incoming [Message]s that won't be
     * handled unless the [RSPlayer] is controlled by a [RSClient] user.
     */
    open fun handleMessages() {
    }

    /**
     * Default method to write [Message]s to the attached channel that won't
     * be handled unless the [RSPlayer] is controlled by a [RSClient] user.
     */
    open fun write(vararg messages: Message) {
    }

    open fun write(vararg messages: Any) {
    }

    /**
     * Default method to flush the attached channel. Won't be handled unless
     * the [RSPlayer] is controlled by a [RSClient] user.
     */
    open fun channelFlush() {
    }

    /**
     * Default method to close the attached channel. Won't be handled unless
     * the [RSPlayer] is controlled by a [RSClient] user.
     */
    open fun channelClose() {
    }

    /**
     * Write a [MessageGameMessage] to the client.
     */
    internal fun writeMessage(message: String) {
        write(MessageGameMessage(type = 0, message = message, username = null))
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
            .add("name", username)
            .add("pid", index)
            .toString()

    /////////////////////// PACKETS /////////////////////////////
    override fun setComponentText(interfaceId: Int, component: Int, text: String) {
        write(IfSetTextMessage(interfaceId, component, text))
    }

    override fun runClientScript(id: Int, vararg args: Any) {
        write(RunClientScriptMessage(id, *args))
    }

    override fun setVarbit(id: Int, value: Int) {
        val def = world.definitions.get(VarbitDef::class.java, id)
        varps.setBit(def.varp, def.startBit, def.endBit, value)
    }

    override fun getVarbit(id: Int): Int {
        val def = world.definitions.get(VarbitDef::class.java, id)
        return varps.getBit(def.varp, def.startBit, def.endBit)
    }

    override fun getVarp(id: Int): Int = varps.getState(id)

    override fun setVarp(id: Int, value: Int) {
        varps.setState(id, value)
    }

    override fun toggleVarp(id: Int) {
        varps.setState(id, varps.getState(id) xor 1)
    }

    override fun syncVarp(id: Int) {
        setVarp(id, getVarp(id))
    }

    override fun addOption(option: String, id: Int, leftClick: Boolean) {
        check(id in 1..options.size) { "Option id must range from [1-${options.size}]" }
        val index = id - 1
        options[index] = option
        write(SetOpPlayerMessage(option, index, leftClick))
    }

    override fun sendRunEnergy(energy: Int) {
        write(UpdateRunEnergyMessage(energy))
    }

    override fun getAppearance(): Appearance {
        return appearance
    }

    /**
     * Write a varbit message to the player's client without actually modifying
     * its varp value in [Player.varps].
     */
    override fun sendTempVarbit(id: Int, value: Int) {
        val def = world.definitions.get(VarbitDef::class.java, id)
        val state = BitManipulation.setBit(varps.getState(def.varp), def.startBit, def.endBit, value)
        val message = if (state in -Byte.MAX_VALUE..Byte.MAX_VALUE) VarpSmallMessage(def.varp, state) else VarpLargeMessage(def.varp, state)
        write(message)
    }

    override fun setInterfaceEvents(interfaceId: Int, component: Int, from: Int, to: Int, setting: Int) {
        write(IfSetEventsMessage(hash = ((interfaceId shl 16) or component), fromChild = from, toChild = to, setting = setting))
    }

    override fun setInterfaceEvents(interfaceId: Int, component: Int, range: IntRange, setting: Int) {
        write(IfSetEventsMessage(hash = ((interfaceId shl 16) or component), fromChild = range.start, toChild = range.endInclusive, setting = setting))
    }

    //////////////////////////////////////////////////////////////

    override fun setAppearance(looks: Appearance) {
        appearance = looks as RSAppearance
    }

    override fun message(message: String, type: ChatMessageType, username: String?) {
        write(MessageGameMessage(type = type.id, message = message, username = username))
    }

    override fun filterableMessage(message: String) {
        write(MessageGameMessage(type = ChatMessageType.SPAM.id, message = message, username = null))
    }

    override fun toggleVarbit(id: Int) {
        val def = world.definitions.get(VarbitDef::class.java, id)
        varps.setBit(def.varp, def.startBit, def.endBit, getVarbit(id) xor 1)
    }

    companion object {
        /**
         * How many tiles a player can 'see' at a time, normally.
         */
        const val NORMAL_VIEW_DISTANCE = 15

        /**
         * How many tiles a player can 'see' at a time when in a 'large' viewport.
         */
        const val LARGE_VIEW_DISTANCE = 127

        /**
         * How many tiles in each direction a player can see at a given time.
         * This should be as far as players can see entities such as ground items
         * and objects.
         */
        const val TILE_VIEW_DISTANCE = 128
    }
}
