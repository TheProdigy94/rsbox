package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.DetectModifiedClientMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSClient

/**
 * @author Tom <rspsmods@gmail.com>
 */
class DetectModifiedClientHandler : MessageHandler<DetectModifiedClientMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: DetectModifiedClientMessage) {
        log(client, "Detected modified client for player %s (%s).", client.username, client.channel)
    }
}