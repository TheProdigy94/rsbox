package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.IfSetAnimMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class IfSetAnimEncoder : MessageEncoder<IfSetAnimMessage>() {

    override fun extract(message: IfSetAnimMessage, key: String): Number = when (key) {
        "hash" -> message.hash
        "anim" -> message.anim
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: IfSetAnimMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}