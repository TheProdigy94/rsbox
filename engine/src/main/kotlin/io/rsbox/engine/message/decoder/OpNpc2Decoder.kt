package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpNpc2Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpNpc2Decoder : MessageDecoder<OpNpc2Message>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpNpc2Message {
        val index = values["index"]!!.toInt()
        val movement = values["movement_type"]!!.toInt()
        return OpNpc2Message(index, movement)
    }
}