package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.DetectModifiedClientMessage
import io.rsbox.engine.model.World
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class DetectModifiedClientHandler : MessageHandler<DetectModifiedClientMessage> {

    override fun handle(client: Client, world: World, message: DetectModifiedClientMessage) {
        log(client, "Detected modified client for player %s (%s).", client.username, client.channel)
    }
}