package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.LogoutFullMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class LogoutFullEncoder : MessageEncoder<LogoutFullMessage>() {

    override fun extract(message: LogoutFullMessage, key: String): Number = throw Exception("Unhandled value key.")

    override fun extractBytes(message: LogoutFullMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}