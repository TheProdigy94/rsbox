package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.EventAppletFocusMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSClient

/**
 * @author Tom <rspsmods@gmail.com>
 */
class EventAppletFocusHandler : MessageHandler<EventAppletFocusMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: EventAppletFocusMessage) {
        when (message.state) {
            FOCUSED_STATE -> client.appletFocused = true
            UNFOCUSED_STATE -> client.appletFocused = false
        }
    }

    companion object {
        private const val UNFOCUSED_STATE = 0
        private const val FOCUSED_STATE = 1
    }
}