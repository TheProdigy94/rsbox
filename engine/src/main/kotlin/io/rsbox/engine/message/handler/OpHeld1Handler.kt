package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpHeld1Message
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_ITEM
import io.rsbox.api.INTERACTING_ITEM_ID
import io.rsbox.api.INTERACTING_ITEM_SLOT
import io.rsbox.api.item.Item
import io.rsbox.engine.model.entity.Client
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpHeld1Handler : MessageHandler<OpHeld1Message> {

    override fun handle(client: Client, world: RSWorld, message: OpHeld1Message) {
        @Suppress("unused")
        val componentParent = message.componentHash shr 16
        @Suppress("unused")
        val componentChild = message.componentHash and 0xFFFF

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

        log(client, "RSItem action 1: id=%d, slot=%d, component=(%d, %d), inventory=(%d, %d)",
                message.item, message.slot, componentParent, componentChild, item.id, item.amount)

        client.attr[INTERACTING_ITEM] = WeakReference(item as Item)
        client.attr[INTERACTING_ITEM_ID] = item.id
        client.attr[INTERACTING_ITEM_SLOT] = message.slot

        if (!world.plugins.executeItem(client, item.id, 1) && world.devContext.debugItemActions) {
            client.writeMessage("Unhandled item action: [item=${item.id}, slot=${message.slot}, option=1]")
        }
    }
}