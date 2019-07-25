package io.rsbox.engine.message.handler

import io.rsbox.api.*
import io.rsbox.api.entity.GroundItem
import io.rsbox.api.item.Item
import io.rsbox.engine.action.GroundItemPathAction
import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpObjUMessage
import io.rsbox.engine.model.EntityType
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSClient
import io.rsbox.engine.model.entity.RSGroundItem
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.model.priv.Privilege
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpObjUHandler : MessageHandler<OpObjUMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: OpObjUMessage) {
        /*
         * If tile is too far away, don't process it.
         */
        val tile = RSTile(message.x, message.z, client.tile.height)
        if (!tile.viewableFrom(client.tile as RSTile, RSPlayer.TILE_VIEW_DISTANCE)) {
            return
        }

        if (!client.lock.canGroundItemInteract() || !client.lock.canItemInteract()) {
            return
        }

        val item = client.inventory[message.slot] ?: return

        if (item.id != message.item) {
            return
        }

        val chunk = world.chunks.getOrCreate(tile)
        val groundItem = chunk.getEntities<RSGroundItem>(tile, EntityType.GROUND_ITEM).firstOrNull { it.item == message.groundItem && it.canBeViewedBy(client) } ?: return

        log(client, "RSItem on Ground RSItem action: item=[%d, %d], ground=[%d, %d], x=%d, z=%d, movement=%d",
                item.id, item.amount, groundItem.item, groundItem.amount, tile.x, tile.z, message.movementType)

        if (message.movementType == 1 && world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            client.moveTo(groundItem.tile as RSTile)
        }

        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        client.attr[INTERACTING_ITEM] = WeakReference(item as Item)
        client.attr[INTERACTING_ITEM_ID] = item.id
        client.attr[INTERACTING_ITEM_SLOT] = message.slot
        client.attr[INTERACTING_OPT_ATTR] = GroundItemPathAction.ITEM_ON_GROUND_ITEM_OPTION
        client.attr[INTERACTING_GROUNDITEM_ATTR] = WeakReference(groundItem as GroundItem)
        client.executePlugin(GroundItemPathAction.walkPlugin)
    }
}