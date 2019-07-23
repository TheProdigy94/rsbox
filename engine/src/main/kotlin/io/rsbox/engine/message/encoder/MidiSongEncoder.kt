package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.MidiSongMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class MidiSongEncoder : MessageEncoder<MidiSongMessage>() {

    override fun extract(message: MidiSongMessage, key: String): Number = when (key) {
        "id" -> message.id
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: MidiSongMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}