package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.ResumePObjDialogMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ResumePObjDialogHandler : MessageHandler<ResumePObjDialogMessage> {

    override fun handle(client: Client, world: RSWorld, message: ResumePObjDialogMessage) {
        log(client, "Searched item: item=%d", message.item)
        client.queues.submitReturnValue(message.item)
    }
}