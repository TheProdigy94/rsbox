package io.rsbox.engine.message.encoder

import io.rsbox.engine.message.MessageEncoder
import io.rsbox.engine.message.impl.TriggerOnDialogAbortMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OnDialogAbortEncoder : MessageEncoder<TriggerOnDialogAbortMessage>() {

    override fun extract(message: TriggerOnDialogAbortMessage, key: String): Number = throw Exception("Unhandled value key.")

    override fun extractBytes(message: TriggerOnDialogAbortMessage, key: String): ByteArray = throw Exception("Unhandled value key.")
}