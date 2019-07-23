package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.ResumePCountDialogMessage
import io.rsbox.engine.model.World
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ResumePCountDialogHandler : MessageHandler<ResumePCountDialogMessage> {

    override fun handle(client: Client, world: World, message: ResumePCountDialogMessage) {
        log(client, "Integer input dialog: input=%d", message.input)
        client.queues.submitReturnValue(Math.max(0, message.input))
    }
}