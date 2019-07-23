package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.ResumePNameDialogMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ResumePNameDialogDecoder : MessageDecoder<ResumePNameDialogMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): ResumePNameDialogMessage {
        val name = stringValues["name"]!!
        return ResumePNameDialogMessage(name)
    }
}