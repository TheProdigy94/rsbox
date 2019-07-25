package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.WindowStatusMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.DISPLAY_MODE_CHANGE_ATTR
import io.rsbox.engine.model.entity.RSClient

/**
 * @author Tom <rspsmods@gmail.com>
 */
class WindowStatusHandler : MessageHandler<WindowStatusMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: WindowStatusMessage) {
        client.clientWidth = message.width
        client.clientHeight = message.height
        client.attr[DISPLAY_MODE_CHANGE_ATTR] = message.mode
        world.plugins.executeWindowStatus(client)
    }
}