package io.rsbox.engine.oldplugin

import com.google.gson.GsonBuilder
import io.rsbox.engine.RSServer
import io.rsbox.engine.event.Event
import io.rsbox.engine.fs.def.ItemDef
import io.rsbox.engine.fs.def.NpcDef
import io.rsbox.engine.fs.def.ObjectDef
import io.rsbox.api.Direction
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.combat.NpcCombatDef
import io.rsbox.engine.model.container.key.ContainerKey
import io.rsbox.engine.model.entity.DynamicObject
import io.rsbox.engine.model.entity.RSGroundItem
import io.rsbox.engine.model.entity.RSNpc
import io.rsbox.engine.model.npcdrops.NpcDropTableDef
import io.rsbox.engine.model.shop.PurchasePolicy
import io.rsbox.engine.model.shop.RSShop
import io.rsbox.engine.model.shop.ShopCurrency
import io.rsbox.engine.model.shop.StockType
import io.rsbox.engine.model.timer.TimerKey
import io.rsbox.engine.service.Service
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.script.experimental.annotations.KotlinScript

/**
 * Represents a KotlinScript oldplugin.
 *
 * @author Tom <rspsmods@gmail.com>
 */
@KotlinScript(
        displayName = "Kotlin Plugin",
        fileExtension = "oldplugin.kts",
        compilationConfiguration = KotlinPluginConfiguration::class
)
abstract class KotlinPlugin(private val r: PluginRepository, val world: RSWorld, val server: RSServer) {

    /**
     * A map of properties that will be copied from the [PluginMetadata] and
     * exposed to the oldplugin.
     */
    private lateinit var properties: MutableMap<String, Any>

    /**
     * Get property associated with [key] casted as [T].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getProperty(key: String): T? = properties[key] as? T?

    /**
     * Set the [PluginMetadata] for this oldplugin.
     */
    fun load_metadata(metadata: PluginMetadata) {
        checkNotNull(metadata.propertyFileName) { "Property file name must be set in order to load metadata." }

        val file = METADATA_PATH.resolve("${metadata.propertyFileName}.json")
        val gson = GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        if (!Files.exists(file)) {
            Files.createDirectories(METADATA_PATH)
            Files.newBufferedWriter(file).use { writer ->
                gson.toJson(metadata, PluginMetadata::class.java, writer)
            }
        }

        Files.newBufferedReader(file).use { reader ->
            val data = gson.fromJson(reader, PluginMetadata::class.java)
            if (data.properties.isNotEmpty()) {
                properties = mutableMapOf()
                data.properties.forEach { key, value ->
                    properties[key] = if (value is Double) value.toInt() else value
                }
            }
        }
    }

    /**
     * Load [service] on oldplugin start-up.
     */
    fun load_service(service: Service) {
        r.services.add(service)
    }

    /**
     * Set the [io.rsbox.engine.model.region.ChunkCoords] with [chunk] as its
     * [io.rsbox.engine.model.region.ChunkCoords.hashCode], as a multi-combat area.
     */
    fun set_multi_combat_chunk(chunk: Int) {
        r.multiCombatChunks.add(chunk)
    }

    /**
     * Set the 8x8 [io.rsbox.engine.model.region.ChunkCoords]s that belong to [region]
     * as multi-combat areas.
     */
    fun set_multi_combat_region(region: Int) {
        r.multiCombatRegions.add(region)
    }

    /**
     * Set the [NpcCombatDef] for npcs with [RSNpc.id] of [npc].
     */
    fun set_combat_def(npc: Int, def: NpcCombatDef) {
        check(!r.npcCombatDefs.containsKey(npc)) { "RSNpc combat definition has been previously set: $npc" }
        r.npcCombatDefs[npc] = def
    }

    /**
     * Set the [NpcDropTableDef] for the npcs with [RSNpc.id] of [RSNpc]
     */
    fun set_drop_table(npcId: Int, def: NpcDropTableDef) {
        check(!r.npcDropTableDefs.containsKey(npcId)) { "RSNpc drop table definition has been previous set: $npcId" }
        r.npcDropTableDefs[npcId] = def
    }

    /**
     * Set the [NpcCombatDef] for npcs with [RSNpc.id] of [npc] and [others].
     */
    fun set_combat_def(npc: Int, vararg others: Int, def: NpcCombatDef) {
        set_combat_def(npc, def)
        others.forEach { other -> set_combat_def(other, def) }
    }

    /**
     * Create a [RSShop] in our world.
     */
    fun create_shop(name: String, currency: ShopCurrency, stockType: StockType = StockType.NORMAL,
                    stockSize: Int = RSShop.DEFAULT_STOCK_SIZE, purchasePolicy: PurchasePolicy = PurchasePolicy.BUY_TRADEABLES,
                    init: RSShop.() -> Unit) {
        val shop = RSShop(name, stockType, purchasePolicy, currency, arrayOfNulls(stockSize))
        r.shops[name] = shop
        init(shop)
    }

    /**
     * Create a [ContainerKey] to register to the [RSWorld] for serialization
     * later on.
     */
    fun register_container_key(key: ContainerKey) {
        r.containerKeys.add(key)
    }

    /**
     * Spawn an [RSNpc] on the given coordinates.
     */
    fun spawn_npc(npc: Int, x: Int, z: Int, height: Int = 0, walkRadius: Int = 0, direction: Direction = Direction.SOUTH): RSNpc {
        val n = RSNpc(npc, RSTile(x, z, height), world)
        n.respawns = true
        n.walkRadius = walkRadius
        n.lastFacingDirection = direction
        r.npcSpawns.add(n)
        return n
    }

    /**
     * Spawn a [DynamicObject] on the given coordinates.
     */
    fun spawn_obj(obj: Int, x: Int, z: Int, height: Int = 0, type: Int = 10, rot: Int = 0) {
        val o = DynamicObject(obj, type, rot, RSTile(x, z, height))
        r.objSpawns.add(o)
    }

    /**
     * Spawn a [RSGroundItem] on the given coordinates.
     */
    fun spawn_item(item: Int, amount: Int, x: Int, z: Int, height: Int = 0, respawnCycles: Int = RSGroundItem.DEFAULT_RESPAWN_CYCLES) {
        val ground = RSGroundItem(item, amount, RSTile(x, z, height))
        ground.respawnCycles = respawnCycles
        r.itemSpawns.add(ground)
    }

    /**
     * Invoke [logic] when the [option] option is clicked on an inventory
     * [io.rsbox.engine.model.item.RSItem].
     *
     * This method should be used over the option-int variant whenever possible.
     */
    fun on_item_option(item: Int, option: String, logic: (Plugin).() -> Unit) {
        val opt = option.toLowerCase()
        val def = world.definitions.get(ItemDef::class.java, item)
        val slot = def.inventoryMenu.indexOfFirst { it?.toLowerCase() == opt }

        check(slot != -1) { "Option \"$option\" not found for item $item [options=${def.inventoryMenu.filterNotNull().filter { it.isNotBlank() }}]" }

        r.bindItem(item, slot + 1, logic)
    }

    /**
     * Invoke [logic] when the [option] option is clicked on an equipment
     * [io.rsbox.engine.model.item.RSItem].
     */
    fun on_equipment_option(item: Int, option: String, logic: (Plugin).() -> Unit) {
        val opt = option.toLowerCase()
        val def = world.definitions.get(ItemDef::class.java, item)
        val slot = def.equipmentMenu.indexOfFirst { it?.toLowerCase() == opt }

        check(slot != -1) { "Option \"$option\" not found for item equipment $item [options=${def.equipmentMenu.filterNotNull().filter { it.isNotBlank() }}]" }

        r.bindEquipmentOption(item, slot + 1, logic)
    }

    /**
     * Invoke [logic] when the [option] option is clicked on a
     * [io.rsbox.engine.model.entity.RSGameObject].
     *
     * This method should be used over the option-int variant whenever possible.
     */
    fun on_obj_option(obj: Int, option: String, lineOfSightDistance: Int = -1, logic: (Plugin).() -> Unit) {
        val opt = option.toLowerCase()
        val def = world.definitions.get(ObjectDef::class.java, obj)
        val slot = def.options.indexOfFirst { it?.toLowerCase() == opt }

        check(slot != -1) { "Option \"$option\" not found for object $obj [options=${def.options.filterNotNull().filter { it.isNotBlank() }}]" }

        r.bindObject(obj, slot + 1, lineOfSightDistance, logic)
    }

    /**
     * Invoke [logic] when the [option] option is clicked on an [RSNpc].
     *
     * This method should be used over the option-int variant whenever possible.
     *
     * @param lineOfSightDistance
     * If the npc is behind an object such as a prison cell or bank booth, this
     * distance should be set. If the npc can be reached normally, you shouldn't
     * specify this value.
     */
    fun on_npc_option(npc: Int, option: String, lineOfSightDistance: Int = -1, logic: (Plugin).() -> Unit) {
        val opt = option.toLowerCase()
        val def = world.definitions.get(NpcDef::class.java, npc)
        val slot = def.options.indexOfFirst { it?.toLowerCase() == opt }

        check(slot != -1) { "Option \"$option\" not found for npc $npc [options=${def.options.filterNotNull().filter { it.isNotBlank() }}]" }

        r.bindNpc(npc, slot + 1, lineOfSightDistance, logic)
    }

    /**
     * Invoke [logic] when [option] option is clicked on a [RSGroundItem].
     *
     * This method should be used over the option-int variant whenever possible.
     */
    fun on_ground_item_option(item: Int, option: String, logic: (Plugin).() -> Unit) {
        val opt = option.toLowerCase()
        val def = world.definitions.get(ItemDef::class.java, item)
        val slot = def.groundMenu.indexOfFirst { it?.toLowerCase() == opt }

        check(slot != -1) { "Option \"$option\" not found for ground item $item [options=${def.groundMenu.filterNotNull().filter { it.isNotBlank() }}]" }

        r.bindGroundItem(item, slot + 1, logic)
    }

    /**
     * Invoke [logic] when an [item] is used on a [io.rsbox.engine.model.entity.RSGameObject]
     *
     * @param obj the game object id
     * @param item the item id
     */
    fun on_item_on_obj(obj: Int, item: Int, lineOfSightDistance: Int = -1, logic: (Plugin).() -> Unit) {
        r.bindItemOnObject(obj, item, lineOfSightDistance, logic)
    }

    /**
     * Invoke [plugin] when [item1] is used on [item2] or vise-versa.
     */
    fun on_item_on_item(item1: Int, item2: Int, plugin: Plugin.() -> Unit) = r.bindItemOnItem(item1, item2, plugin)

    /**
     * Invoke [plugin] when [item] in inventory is used on [groundItem] on ground.
     */
    fun on_item_on_ground_item(item: Int, groundItem: Int, plugin: Plugin.() -> Unit) = r.bindItemOnGroundItem(item, groundItem, plugin)

    /**
     * Set the logic to execute when [io.rsbox.engine.message.impl.WindowStatusMessage]
     * is handled.
     */
    fun set_window_status_logic(logic: (Plugin).() -> Unit) = r.bindWindowStatus(logic)

    /**
     * Set the logic to execute when [io.rsbox.engine.message.impl.CloseModalMessage]
     * is handled.
     */
    fun set_modal_close_logic(logic: (Plugin).() -> Unit) = r.bindModalClose(logic)

    /**
     * Set the logic to check if a player has a menu opened and any [io.rsbox.engine.model.queue.QueueTask]
     * with a [io.rsbox.engine.model.queue.TaskPriority.STANDARD] priority should wait before executing.
     *
     * @see PluginRepository.isMenuOpenedPlugin
     *
     * @return
     * True if the player has a menu opened and any standard task should wait
     * before executing.
     */
    fun set_menu_open_check(logic: Plugin.() -> Boolean) = r.setMenuOpenedCheck(logic)

    /**
     * Set the logic to execute by default when [io.rsbox.engine.model.entity.RSPawn.attack]
     * is handled.
     */
    fun set_combat_logic(logic: (Plugin).() -> Unit) = r.bindCombat(logic)

    /**
     * Set the logic to execute when a player levels a skill.
     */
    fun set_level_up_logic(logic: (Plugin).() -> Unit) = r.bindSkillLevelUp(logic)

    /**
     * Invoke [logic] when [RSWorld.postLoad] is handled.
     */
    fun on_world_init(logic: (Plugin).() -> Unit) = r.bindWorldInit(logic)

    /**
     * Invoke [logic] when an [Event] is triggered.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Event> on_event(event: Class<out T>, logic: Plugin.(T) -> Unit) = r.bindEvent(event, logic as Plugin.(Event) -> Unit)

    /**
     * Invoke [logic] on player log in.
     */
    fun on_login(logic: (Plugin).() -> Unit) = r.bindLogin(logic)

    /**
     * Invoke [logic] on player log out.
     */
    fun on_logout(logic: (Plugin).() -> Unit) = r.bindLogout(logic)

    /**
     * Invoked when an item is swapped on the same component.
     */
    fun on_component_item_swap(interfaceId: Int, component: Int, plugin: Plugin.() -> Unit) = r.bindComponentItemSwap(interfaceId, component, plugin)

    /**
     * Invoked when an item is swapped between two components.
     */
    fun on_component_to_component_item_swap(srcInterfaceId: Int, srcComponent: Int, dstInterfaceId: Int, dstComponent: Int, plugin: Plugin.() -> Unit) = r.bindComponentToComponentItemSwap(srcInterfaceId, srcComponent, dstInterfaceId, dstComponent, plugin)

    /**
     * Invokes when a player interaction option is executed
     */
    fun on_player_option(option: String, plugin: Plugin.() -> Unit) = r.bindPlayerOption(option, plugin)

    /**
     * Invoked when a player hits 0 hp and is starting their death task.
     */
    fun on_player_pre_death(plugin: Plugin.() -> Unit) = r.bindPlayerPreDeath(plugin)

    /**
     * Invoked when a player is sent back to their respawn location on
     * death.
     */
    fun on_player_death(plugin: Plugin.() -> Unit) = r.bindPlayerDeath(plugin)

    /**
     * Invoked when npc with [RSNpc.id] of [npc] invokes their death task.
     */
    fun on_npc_pre_death(npc: Int, plugin: Plugin.() -> Unit) = r.bindNpcPreDeath(npc, plugin)

    /**
     * Invoked when npc with [RSNpc.id] of [npc] finishes their death task and
     * is de-registered from the world.
     */
    fun on_npc_death(npc: Int, plugin: Plugin.() -> Unit) = r.bindNpcDeath(npc, plugin)

    /**
     * Set the combat logic for [npc] and [others], which will override the [set_combat_logic]
     * logic.
     */
    fun on_npc_combat(npc: Int, vararg others: Int, logic: (Plugin).() -> Unit) {
        r.bindNpcCombat(npc, logic)
        others.forEach { other -> r.bindNpcCombat(other, logic) }
    }

    /**
     * Invoke [logic] when [io.rsbox.engine.message.impl.OpNpcTMessage] is handled.
     */
    fun on_spell_on_npc(parent: Int, child: Int, logic: (Plugin).() -> Unit) = r.bindSpellOnNpc(parent, child, logic)

    /**
     * Invoke [logic] when [io.rsbox.engine.message.impl.IfOpenSubMessage] is handled.
     */
    fun on_interface_open(interfaceId: Int, logic: (Plugin).() -> Unit) = r.bindInterfaceOpen(interfaceId, logic)

    /**
     * Invoke [logic] when [io.rsbox.engine.model.interf.InterfaceSet.closeByHash]
     * is handled.
     */
    fun on_interface_close(interfaceId: Int, logic: (Plugin).() -> Unit) = r.bindInterfaceClose(interfaceId, logic)

    /**
     * Invoke [logic] when [io.rsbox.engine.message.impl.IfButtonMessage] is handled.
     */
    fun on_button(interfaceId: Int, component: Int, logic: (Plugin).() -> Unit) = r.bindButton(interfaceId, component, logic)

    /**
     * Invoke [logic] when [key] reaches a time value of 0.
     */
    fun on_timer(key: TimerKey, logic: (Plugin).() -> Unit) = r.bindTimer(key, logic)

    /**
     * Invoke [logic] when any npc is spawned into the game with [RSWorld.spawn].
     */
    fun on_global_npc_spawn(logic: (Plugin).() -> Unit) = r.bindGlobalNpcSpawn(logic)

    /**
     * Invoke [logic] when a ground item is picked up by a [io.rsbox.engine.model.entity.RSPlayer].
     */
    fun on_global_item_pickup(logic: Plugin.() -> Unit) = r.bindGlobalGroundItemPickUp(logic)

    /**
     * Invoke [logic] when an npc with [RSNpc.id] matching [npc] is spawned into
     * the game with [RSWorld.spawn].
     */
    fun on_npc_spawn(npc: Int, logic: (Plugin).() -> Unit) = r.bindNpcSpawn(npc, logic)

    /**
     * Invoke [logic] when [io.rsbox.engine.message.impl.ClientCheatMessage] is handled.
     */
    fun on_command(command: String, powerRequired: String? = null, logic: (Plugin).() -> Unit) = r.bindCommand(command, powerRequired, logic)

    /**
     * Invoke [logic] when an item is equipped onto equipment slot [equipSlot].
     */
    fun on_equip_to_slot(equipSlot: Int, logic: (Plugin).() -> Unit) = r.bindEquipSlot(equipSlot, logic)

    /**
     * Invoke [logic] when an item is un-equipped from equipment slot [equipSlot].
     */
    fun on_unequip_from_slot(equipSlot: Int, logic: (Plugin).() -> Unit) = r.bindUnequipSlot(equipSlot, logic)

    /**
     * Return true if [item] can be equipped, false if it can't.
     */
    fun can_equip_item(item: Int, logic: (Plugin).() -> Boolean) = r.bindEquipItemRequirement(item, logic)

    /**
     * Invoke [logic] when [item] is equipped.
     */
    fun on_item_equip(item: Int, logic: (Plugin).() -> Unit) = r.bindEquipItem(item, logic)

    /**
     * Invoke [logic] when [item] is removed from equipment.
     */
    fun on_item_unequip(item: Int, logic: (Plugin).() -> Unit) = r.bindUnequipItem(item, logic)

    /**
     * Invoke [logic] when a player enters a region (8x8 Chunks).
     */
    fun on_enter_region(regionId: Int, logic: (Plugin).() -> Unit) = r.bindRegionEnter(regionId, logic)

    /**
     * Invoke [logic] when a player exits a region (8x8 Chunks).
     */
    fun on_exit_region(regionId: Int, logic: (Plugin).() -> Unit) = r.bindRegionExit(regionId, logic)

    /**
     * Invoke [logic] when a player enters a chunk (8x8 Tiles).
     */
    fun on_enter_chunk(chunkHash: Int, logic: (Plugin).() -> Unit) = r.bindChunkEnter(chunkHash, logic)

    /**
     * Invoke [logic] when a player exits a chunk (8x8 Tiles).
     */
    fun on_exit_chunk(chunkHash: Int, logic: (Plugin).() -> Unit) = r.bindChunkExit(chunkHash, logic)

    /**
     * Invoke [logic] when the the option in index [option] is clicked on an inventory item.
     *
     * String option method should be used over this method whenever possible.
     */
    fun on_item_option(item: Int, option: Int, logic: (Plugin).() -> Unit) = r.bindItem(item, option, logic)

    /**
     * Invoke [logic] when the the option in index [option] is clicked on a
     * [io.rsbox.engine.model.entity.RSGameObject].
     *
     * String option method should be used over this method whenever possible.
     *
     * @param lineOfSightDistance
     * If the npc is behind an object such as a prison cell or bank booth, this
     * distance should be set. If the npc can be reached normally, you shouldn't
     * specify this value.
     */
    fun on_obj_option(obj: Int, option: Int, lineOfSightDistance: Int = -1, logic: (Plugin).() -> Unit) = r.bindObject(obj, option, lineOfSightDistance, logic)

    /**
     * Invoke [logic] when the the option in index [option] is clicked on an [RSNpc].
     *
     * String option method should be used over this method whenever possible.
     */
    fun on_npc_option(npc: Int, option: Int, lineOfSightDistance: Int = -1, logic: (Plugin).() -> Unit) = r.bindNpc(npc, option, lineOfSightDistance, logic)

    /**
     * Invoke [logic] when the the option in index [option] is clicked on a [RSGroundItem].
     *
     * String option method should be used over this method whenever possible.
     */
    fun on_ground_item_option(item: Int, option: Int, logic: (Plugin).() -> Unit) = r.bindGroundItem(item, option, logic)

    /**
     * Set the condition of whether [item] can be picked up as a ground item.
     *
     * @return false if the item can not be picked up.
     */
    fun set_ground_item_condition(item: Int, plugin: Plugin.() -> Boolean) = r.setGroundItemPickupCondition(item, plugin)

    /**
     * Invoke [plugin] when a spell is used on an item.
     */
    fun on_spell_on_item(fromInterface: Int, fromComponent: Int, toInterface: Int, toComponent: Int, plugin: Plugin.() -> Unit) = r.bindSpellOnItem((fromInterface shl 16) or fromComponent, (toInterface shl 16) or toComponent, plugin)

    /**
     * Returns true if the item can be dropped on the floor via the 'drop' menu
     * option - return false otherwise.
     */
    fun can_drop_item(item: Int, plugin: (Plugin).() -> Boolean) = r.bindCanItemDrop(item, plugin)

    /**
     * Invoke [plugin] when [item] is used on [npc].
     */
    fun on_item_on_npc(item: Int, npc: Int, plugin: Plugin.() -> Unit) = r.bindItemOnNpc(npc = npc, item = item, plugin = plugin)

    /**
     * Invoke [plugin] when player starts fishing at a spot
     */
    fun on_start_fishing(spotId: Int, plugin: Plugin.() -> Unit) = r.bindOnStartFishing(npc_spot = spotId, plugin = plugin)

    /**
     * Invoke [plugin] when player catches a fish at a spot
     */
    fun on_catch_fish(npc_spot: Int, plugin: Plugin.() -> Unit) = r.bindOnCatchFish(npc_spot = npc_spot, plugin = plugin)

    companion object {
        private val METADATA_PATH = Paths.get("./plugins", "configs")
    }
}