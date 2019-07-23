package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.SetCameraAngleMessage

class SetCameraAngleEncoder : MessageEncoder<SetCameraAngleMessage>() {
    override fun extract(message: SetCameraAngleMessage, key: String): Number = when(key) {
        "localX" -> message.localX
        "localZ" -> message.localZ
        "localY" -> message.localY
        "slowdownSpeed" -> message.slowdownSpeed
        "speed" -> message.speed
        else -> throw Exception("Unhandled key value.")
    }

    override fun extractBytes(message: SetCameraAngleMessage, key: String): ByteArray = throw Exception("Unhandled key value.")
}