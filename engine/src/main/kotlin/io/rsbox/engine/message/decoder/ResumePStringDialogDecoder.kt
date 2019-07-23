package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.ResumePStringDialogMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ResumePStringDialogDecoder : MessageDecoder<ResumePStringDialogMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): ResumePStringDialogMessage {
        val input = stringValues["input"]!!
        return ResumePStringDialogMessage(input)
    }
}