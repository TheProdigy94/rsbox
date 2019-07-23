package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.UpdateStatMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class UpdateStatEncoder : MessageEncoder<UpdateStatMessage>() {

    override fun extract(message: UpdateStatMessage, key: String): Number = when (key) {
        "level" -> message.level
        "xp" -> message.xp
        "skill" -> message.skill
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: UpdateStatMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}