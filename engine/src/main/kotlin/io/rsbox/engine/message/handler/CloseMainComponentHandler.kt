package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.CloseModalMessage
import io.rsbox.engine.model.World
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class CloseMainComponentHandler : MessageHandler<CloseModalMessage> {

    override fun handle(client: Client, world: World, message: CloseModalMessage) {
        client.closeInterfaceModal()
    }
}