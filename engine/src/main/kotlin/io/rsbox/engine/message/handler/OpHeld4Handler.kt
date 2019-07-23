package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpHeld4Message
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.attr.INTERACTING_ITEM
import io.rsbox.engine.model.attr.INTERACTING_ITEM_ID
import io.rsbox.engine.model.attr.INTERACTING_ITEM_SLOT
import io.rsbox.engine.model.entity.Client
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpHeld4Handler : MessageHandler<OpHeld4Message> {

    override fun handle(client: Client, world: RSWorld, message: OpHeld4Message) {
        @Suppress("unused")
        val interfaceId = message.componentHash shr 16
        @Suppress("unused")
        val component = message.componentHash and 0xFFFF

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

        log(client, "Item action 4: id=%d, slot=%d, component=(%d, %d), inventory=(%d, %d)",
                message.item, message.slot, interfaceId, component, item.id, item.amount)

        client.attr[INTERACTING_ITEM] = WeakReference(item)
        client.attr[INTERACTING_ITEM_ID] = item.id
        client.attr[INTERACTING_ITEM_SLOT] = message.slot

        if (!world.plugins.executeItem(client, item.id, 4) && world.devContext.debugItemActions) {
            client.writeMessage("Unhandled item action: [item=${item.id}, slot=${message.slot}, option=4]")
        }
    }
}