package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.IfOpenSubMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class IfOpenSubEncoder : MessageEncoder<IfOpenSubMessage>() {

    override fun extract(message: IfOpenSubMessage, key: String): Number = when (key) {
        "component" -> message.component
        "overlay" -> (message.parent shl 16) or message.child
        "type" -> message.type
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: IfOpenSubMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}