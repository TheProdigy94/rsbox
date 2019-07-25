package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpHeldTMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_ITEM
import io.rsbox.api.INTERACTING_ITEM_ID
import io.rsbox.api.INTERACTING_ITEM_SLOT
import io.rsbox.api.item.Item
import io.rsbox.engine.model.entity.RSClient
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpHeldTHandler : MessageHandler<OpHeldTMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: OpHeldTMessage) {
        val fromComponentHash = message.fromComponentHash
        val fromInterfaceId = fromComponentHash shr 16
        val fromComponent = fromComponentHash and 0xFFFF

        val toComponentHash = message.toComponentHash
        val toInterfaceId = toComponentHash shr 16
        val toComponent = toComponentHash and 0xFFFF

        val itemId = message.item
        val itemSlot = message.itemSlot
        val unknown = message.spellSlot

        val item = client.inventory[itemSlot] ?: return

        if (item.id != itemId) {
            return
        }

        if (!client.lock.canInterfaceInteract()) {
            return
        }

        log(client, "Magic spell on item: from_component=[%d,%d], to_component=[%d,%d], unknown=%d, item=%d, item_slot=%d",
                fromInterfaceId, fromComponent, toInterfaceId, toComponent, unknown, itemId, itemSlot)

        client.attr[INTERACTING_ITEM] = WeakReference(item as Item)
        client.attr[INTERACTING_ITEM_ID] = itemId
        client.attr[INTERACTING_ITEM_SLOT] = itemSlot

        val handled = world.plugins.executeSpellOnItem(client, fromComponentHash, toComponentHash)
        if (!handled && world.devContext.debugMagicSpells) {
            client.writeMessage("Unhandled spell on item: [item=[${item.id}, ${item.amount}], slot=$itemSlot, unknown=$unknown " +
                    "from_component=[$fromInterfaceId:$fromComponent], to_component=[$toInterfaceId:$toComponent]]")
        }
    }
}