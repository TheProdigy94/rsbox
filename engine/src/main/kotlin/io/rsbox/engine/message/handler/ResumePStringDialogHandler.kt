package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.ResumePStringDialogMessage
import io.rsbox.engine.model.World
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ResumePStringDialogHandler : MessageHandler<ResumePStringDialogMessage> {

    override fun handle(client: Client, world: World, message: ResumePStringDialogMessage) {
        log(client, "String input dialog: input=%s", message.input)
        client.queues.submitReturnValue(message.input)
    }
}