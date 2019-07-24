package io.rsbox.engine.message.handler

import io.rsbox.engine.action.ObjectPathAction
import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpLocUMessage
import io.rsbox.engine.model.EntityType
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_ITEM
import io.rsbox.api.INTERACTING_OBJ_ATTR
import io.rsbox.api.item.Item
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.entity.RSGameObject
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.model.priv.Privilege
import java.lang.ref.WeakReference

/**
 * @author Triston Plummer ("Dread")
 *
 * Handles the usage of an item on an object
 */
class OpLocUHandler : MessageHandler<OpLocUMessage> {

    override fun handle(client: Client, world: RSWorld, message: OpLocUMessage) {
        if (message.slot < 0 || message.slot >= client.inventory.capacity) {
            return
        }

        if (!client.lock.canItemInteract()) {
            return
        }

        val item = client.inventory[message.slot] ?: return

        if (item.id != message.item) {
            return
        }

        // If the player can't move, do nothing
        if (!client.lock.canMove()) {
            return
        }

        // If the tile is too far away, do nothing
        val tile = RSTile(message.x, message.z, client.tile.height)
        if (!tile.viewableFrom(client.tile, RSPlayer.TILE_VIEW_DISTANCE)) {
            return
        }

        // Get the region chunk that the object would belong to.
        val chunk = world.chunks.getOrCreate(tile)
        val obj = chunk.getEntities<RSGameObject>(tile, EntityType.STATIC_OBJECT, EntityType.DYNAMIC_OBJECT).firstOrNull { it.id == message.obj } ?: return

        if (message.movementType == 1 && world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            val def = obj.getDef(world.definitions)
            client.moveTo(world.findRandomTileAround(obj.tile, radius = 1, centreWidth = def.width, centreLength = def.length) ?: obj.tile)
        }

        log(client, "RSItem on object: item=%d, slot=%d, obj=%d, x=%d, z=%d", message.item, message.slot, message.obj, message.x, message.z)

        client.stopMovement()
        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        client.attr[INTERACTING_ITEM] = WeakReference(item as Item)
        client.attr[INTERACTING_OBJ_ATTR] = WeakReference(obj)

        client.executePlugin(ObjectPathAction.itemOnObjectPlugin)
    }
}