package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpHeld6Message
import io.rsbox.engine.model.ExamineEntityType
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpHeld6Handler : MessageHandler<OpHeld6Message> {

    override fun handle(client: Client, world: RSWorld, message: OpHeld6Message) {
        world.sendExamine(client, message.item, ExamineEntityType.ITEM)
    }
}