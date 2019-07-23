package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpNpc6Message
import io.rsbox.engine.model.ExamineEntityType
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpNpc6Handler : MessageHandler<OpNpc6Message> {

    override fun handle(client: Client, world: RSWorld, message: OpNpc6Message) {
        world.sendExamine(client, message.npcId, ExamineEntityType.NPC)
    }
}