package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.SetCameraPosMessage

class SetCameraPosEncoder : MessageEncoder<SetCameraPosMessage>() {
    override fun extract(message: SetCameraPosMessage, key: String): Number = when(key) {
        "cameraX" -> message.cameraX
        "cameraZ" -> message.cameraZ
        "cameraY" -> message.cameraY
        "field4" -> message.field4
        "field5" -> message.field5
        else -> throw Exception("Unhandled value key.")
    }

    override fun extractBytes(message: SetCameraPosMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}