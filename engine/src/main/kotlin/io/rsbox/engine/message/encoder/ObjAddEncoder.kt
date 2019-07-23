package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.ObjAddMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ObjAddEncoder : MessageEncoder<ObjAddMessage>() {

    override fun extract(message: ObjAddMessage, key: String): Number = when (key) {
        "item" -> message.item
        "amount" -> message.amount
        "tile" -> message.tile
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: ObjAddMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}