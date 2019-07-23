package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.ClanJoinChatLeaveChatMessage
import io.rsbox.engine.model.World
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ClanJoinChatLeaveHandler : MessageHandler<ClanJoinChatLeaveChatMessage> {

    override fun handle(client: Client, world: World, message: ClanJoinChatLeaveChatMessage) {
        throw RuntimeException("Unhandled.")
    }
}