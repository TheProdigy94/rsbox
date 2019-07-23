package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpLoc6Message
import io.rsbox.engine.model.ExamineEntityType
import io.rsbox.engine.model.World
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpLoc6Handler : MessageHandler<OpLoc6Message> {

    override fun handle(client: Client, world: World, message: OpLoc6Message) {
        world.sendExamine(client, message.id, ExamineEntityType.OBJECT)
    }
}