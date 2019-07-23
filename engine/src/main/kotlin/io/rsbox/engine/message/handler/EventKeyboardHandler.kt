package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.EventKeyboardMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class EventKeyboardHandler : MessageHandler<EventKeyboardMessage> {

    override fun handle(client: Client, world: RSWorld, message: EventKeyboardMessage) {
    }
}