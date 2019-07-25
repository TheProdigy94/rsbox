package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.MessagePublicMessage
import io.rsbox.engine.model.ChatMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSClient
import io.rsbox.engine.model.priv.Privilege
import io.rsbox.engine.service.log.LoggerService
import io.rsbox.api.UpdateBlockType

/**
 * @author Tom <rspsmods@gmail.com>
 */
class MessagePublicHandler : MessageHandler<MessagePublicMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: MessagePublicMessage) {
        val decompressed = ByteArray(256)
        val huffman = world.huffman
        huffman.decompress(message.data, decompressed, message.length)

        val unpacked = String(decompressed, 0, message.length)
        val type = ChatMessage.ChatType.values.firstOrNull { it.id == message.type } ?: ChatMessage.ChatType.NONE
        val effect = ChatMessage.ChatEffect.values.firstOrNull { it.id == message.effect } ?: ChatMessage.ChatEffect.NONE
        val color = ChatMessage.ChatColor.values.firstOrNull { it.id == message.color } ?: ChatMessage.ChatColor.NONE

        client.blockBuffer.publicChat = ChatMessage(unpacked, Privilege.DEFAULT.icon, type, effect, color)
        client.addBlock(UpdateBlockType.PUBLIC_CHAT)
        world.getService(LoggerService::class.java, searchSubclasses = true)?.logPublicChat(client, unpacked)
    }
}