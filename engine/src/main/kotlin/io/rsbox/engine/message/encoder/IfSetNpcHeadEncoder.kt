package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.IfSetNpcHeadMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class IfSetNpcHeadEncoder : MessageEncoder<IfSetNpcHeadMessage>() {

    override fun extract(message: IfSetNpcHeadMessage, key: String): Number = when (key) {
        "hash" -> message.hash
        "npc" -> message.npc
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: IfSetNpcHeadMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}