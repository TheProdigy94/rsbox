package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.ResumePNameDialogMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSClient
import io.rsbox.engine.model.queue.RSQueueTask

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ResumePNameDialogHandler : MessageHandler<ResumePNameDialogMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: ResumePNameDialogMessage) {
        val name = message.name
        val target = world.getPlayerForName(name)

        log(client, "RSPlayer username input dialog: username=%s", name)

        client.queues.submitReturnValue(target ?: RSQueueTask.EMPTY_RETURN_VALUE)
    }
}