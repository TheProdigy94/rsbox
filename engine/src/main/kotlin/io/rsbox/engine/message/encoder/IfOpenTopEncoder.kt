package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.IfOpenTopMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class IfOpenTopEncoder : MessageEncoder<IfOpenTopMessage>() {

    override fun extract(message: IfOpenTopMessage, key: String): Number = when (key) {
        "top" -> message.top
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: IfOpenTopMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}