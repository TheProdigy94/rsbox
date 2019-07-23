package io.rsbox.engine.message.handler

import io.rsbox.engine.action.GroundItemPathAction
import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpObj3Message
import io.rsbox.engine.model.EntityType
import io.rsbox.engine.model.Tile
import io.rsbox.engine.model.World
import io.rsbox.engine.model.attr.INTERACTING_GROUNDITEM_ATTR
import io.rsbox.engine.model.attr.INTERACTING_OPT_ATTR
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.entity.GroundItem
import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.model.priv.Privilege
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpObj3Handler : MessageHandler<OpObj3Message> {

    override fun handle(client: Client, world: World, message: OpObj3Message) {
        /**
         * If tile is too far away, don't process it.
         */
        val tile = Tile(message.x, message.z, client.tile.height)
        if (!tile.viewableFrom(client.tile, Player.TILE_VIEW_DISTANCE)) {
            return
        }

        if (!client.lock.canGroundItemInteract()) {
            return
        }

        log(client, "Ground Item action 3: item=%d, x=%d, z=%d, movement=%d", message.item, message.x, message.z, message.movementType)

        /**
         * Get the region chunk that the object would belong to.
         */
        val chunk = world.chunks.getOrCreate(tile)
        val item = chunk.getEntities<GroundItem>(tile, EntityType.GROUND_ITEM).firstOrNull { it.item == message.item && it.canBeViewedBy(client) } ?: return

        if (message.movementType == 1 && world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            client.moveTo(item.tile)
        }

        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        client.attr[INTERACTING_OPT_ATTR] = 3
        client.attr[INTERACTING_GROUNDITEM_ATTR] = WeakReference(item)
        client.executePlugin(GroundItemPathAction.walkPlugin)
    }
}