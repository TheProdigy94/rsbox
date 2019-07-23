package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.VarpSmallMessage

/**
 * Responsible for extracting values from the [VarpSmallMessage] based on keys.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class VarpSmallEncoder : MessageEncoder<VarpSmallMessage>() {

    override fun extract(message: VarpSmallMessage, key: String): Number = when (key) {
        "id" -> message.id
        "value" -> message.value
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: VarpSmallMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}