package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.ResumePauseButtonMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ResumePauseButtonDecoder : MessageDecoder<ResumePauseButtonMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): ResumePauseButtonMessage {
        val hash = values["hash"]!!.toInt()
        val slot = values["slot"]!!.toInt()
        return ResumePauseButtonMessage(interfaceId = hash shr 16, component = hash and 0xFFFF, slot = if (slot >= 0xFFFF) -1 else slot)
    }
}