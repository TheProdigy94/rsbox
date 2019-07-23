package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.ObjDelMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ObjDelEncoder : MessageEncoder<ObjDelMessage>() {

    override fun extract(message: ObjDelMessage, key: String): Number = when (key) {
        "item" -> message.item
        "tile" -> message.tile
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: ObjDelMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}