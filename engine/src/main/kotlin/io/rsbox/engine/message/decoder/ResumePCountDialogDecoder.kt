package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.ResumePCountDialogMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ResumePCountDialogDecoder : MessageDecoder<ResumePCountDialogMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): ResumePCountDialogMessage {
        val input = values["input"]!!.toInt()
        return ResumePCountDialogMessage(input)
    }
}