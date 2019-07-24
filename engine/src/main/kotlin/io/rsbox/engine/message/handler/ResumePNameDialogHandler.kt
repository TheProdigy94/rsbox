package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.ResumePNameDialogMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.queue.QueueTaskeTask

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ResumePNameDialogHandler : MessageHandler<ResumePNameDialogMessage> {

    override fun handle(client: Client, world: RSWorld, message: ResumePNameDialogMessage) {
        val name = message.name
        val target = world.getPlayerForName(name)

        log(client, "RSPlayer username input dialog: username=%s", name)

        client.queues.submitReturnValue(target ?: QueueTask.EMPTY_RETURN_VALUE)
    }
}