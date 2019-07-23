package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.ResumePObjDialogMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ResumePObjDialogDecoder : MessageDecoder<ResumePObjDialogMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): ResumePObjDialogMessage {
        val item = values["item"]!!.toInt()
        return ResumePObjDialogMessage(item)
    }
}