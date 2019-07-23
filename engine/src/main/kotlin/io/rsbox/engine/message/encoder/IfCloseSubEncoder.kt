package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.IfCloseSubMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class IfCloseSubEncoder : MessageEncoder<IfCloseSubMessage>() {

    override fun extract(message: IfCloseSubMessage, key: String): Number = when (key) {
        "hash" -> message.hash
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: IfCloseSubMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}