package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.ResumePauseButtonMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ResumePauseButtonHandler : MessageHandler<ResumePauseButtonMessage> {

    override fun handle(client: Client, world: RSWorld, message: ResumePauseButtonMessage) {
        log(client, "Continue dialog: component=[%d:%d], slot=%d", message.interfaceId, message.component, message.slot)
        client.queues.submitReturnValue(message)
    }
}