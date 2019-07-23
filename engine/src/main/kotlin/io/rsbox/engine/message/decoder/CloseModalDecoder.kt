package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.CloseModalMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class CloseModalDecoder : MessageDecoder<CloseModalMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): CloseModalMessage {
        return CloseModalMessage()
    }
}