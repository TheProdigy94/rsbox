package io.rsbox.engine.message.handler

import io.rsbox.engine.action.GroundItemPathAction
import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpObj1Message
import io.rsbox.engine.model.EntityType
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_GROUNDITEM_ATTR
import io.rsbox.api.INTERACTING_OPT_ATTR
import io.rsbox.api.entity.GroundItem
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.entity.RSGroundItem
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.model.priv.Privilege
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpObj1Handler : MessageHandler<OpObj1Message> {

    override fun handle(client: Client, world: RSWorld, message: OpObj1Message) {
        /*
         * If tile is too far away, don't process it.
         */
        val tile = RSTile(message.x, message.z, client.tile.height)
        if (!tile.viewableFrom(client.tile as RSTile, RSPlayer.TILE_VIEW_DISTANCE)) {
            return
        }

        if (!client.lock.canGroundItemInteract()) {
            return
        }

        log(client, "Ground RSItem action 1: item=%d, x=%d, z=%d, movement=%d", message.item, message.x, message.z, message.movementType)

        /*
         * Get the region chunk that the object would belong to.
         */
        val chunk = world.chunks.getOrCreate(tile)
        val item = chunk.getEntities<RSGroundItem>(tile, EntityType.GROUND_ITEM).firstOrNull { it.item == message.item && it.canBeViewedBy(client) } ?: return

        if (message.movementType == 1 && world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            client.moveTo(item.tile as RSTile)
        }

        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        client.attr[INTERACTING_OPT_ATTR] = 1
        client.attr[INTERACTING_GROUNDITEM_ATTR] = WeakReference(item as GroundItem)
        client.executePlugin(GroundItemPathAction.walkPlugin)
    }
}