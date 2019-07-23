package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.UpdateZonePartialFollowsMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class UpdateZonePartialFollowsEncoder : MessageEncoder<UpdateZonePartialFollowsMessage>() {

    override fun extract(message: UpdateZonePartialFollowsMessage, key: String): Number = when (key) {
        "x" -> message.x
        "z" -> message.z
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: UpdateZonePartialFollowsMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}