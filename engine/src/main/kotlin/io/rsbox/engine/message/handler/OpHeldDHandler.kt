package io.rsbox.engine.message.handler

import io.rsbox.engine.game.Game
import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpHeldDMessage
import io.rsbox.engine.model.World
import io.rsbox.engine.model.attr.INTERACTING_ITEM_SLOT
import io.rsbox.engine.model.attr.OTHER_ITEM_SLOT_ATTR
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpHeldDHandler : MessageHandler<OpHeldDMessage> {

    override fun handle(client: Client, world: World, message: OpHeldDMessage) {
        val interfaceId = message.componentHash shr 16
        val component = message.componentHash and 0xFFFF
        val fromSlot = message.srcSlot
        val toSlot = message.dstSlot

        log(client, "Swap component item: component=[%d:%d], src_slot=%d, dst_slot=%d", interfaceId, component, fromSlot, toSlot)

        client.attr[INTERACTING_ITEM_SLOT] = fromSlot
        client.attr[OTHER_ITEM_SLOT_ATTR] = toSlot

        Game.swapItem(client)
    }
}