package io.rsbox.engine.protocol

import io.rsbox.engine.message.Message
import io.rsbox.engine.message.MessageEncoderSet
import io.rsbox.engine.message.MessageStructureSet
import io.rsbox.net.packet.GamePacketBuilder
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import mu.KLogging

/**
 * An implementation of [MessageToMessageEncoder] which is responsible for taking
 * the [Message] and converting it into a [io.rsbox.net.packet.GamePacket] so that
 * it may be written to a [io.netty.channel.Channel].
 *
 * @param encoders
 * The available [io.rsbox.engine.message.MessageEncoder]s for the current
 * [io.rsbox.engine.GameContext].
 *
 * @author Tom <rspsmods@gmail.com>
 */
class GameMessageEncoder(private val encoders: MessageEncoderSet, private val structures: MessageStructureSet) : MessageToMessageEncoder<Message>() {

    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: MutableList<Any>) {
        val encoder = encoders.get(msg.javaClass)
        val structure = structures.get(msg.javaClass)

        if (encoder == null) {
            logger.error("No encoder found for message $msg")
            return
        }

        if (structure == null) {
            logger.error("No packet structure found for message $msg")
            return
        }

        val builder = GamePacketBuilder(structure.opcodes.first(), structure.type)
        encoder.encode(msg, builder, structure)
        out.add(builder.toGamePacket())
    }

    companion object : KLogging()
}