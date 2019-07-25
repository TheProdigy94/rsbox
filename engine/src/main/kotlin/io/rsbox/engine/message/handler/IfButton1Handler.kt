package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.IfButtonMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_ITEM_ID
import io.rsbox.api.INTERACTING_OPT_ATTR
import io.rsbox.api.INTERACTING_SLOT_ATTR
import io.rsbox.engine.model.entity.RSClient

/**
 * @author Tom <rspsmods@gmail.com>
 */
class IfButton1Handler : MessageHandler<IfButtonMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: IfButtonMessage) {
        val interfaceId = message.hash shr 16
        val component = message.hash and 0xFFFF
        val option = message.option + 1

        if (!client.interfaces.isVisible(interfaceId)) {
            return
        }

        log(client, "Click button: component=[%d:%d], option=%d, slot=%d, item=%d", interfaceId, component, option, message.slot, message.item)

        client.attr[INTERACTING_OPT_ATTR] = option
        client.attr[INTERACTING_ITEM_ID] = message.item
        client.attr[INTERACTING_SLOT_ATTR] = message.slot

        if (world.plugins.executeButton(client, interfaceId, component)) {
            return
        }

        if (world.devContext.debugButtons) {
            client.writeMessage("Unhandled button action: [component=[$interfaceId:$component], option=$option, slot=${message.slot}, item=${message.item}]")
        }
    }
}