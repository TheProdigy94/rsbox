package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpPlayer5Message

/**
 * @author Triston Plummer ("Dread")
 */
class OpPlayer5Decoder : MessageDecoder<OpPlayer5Message>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpPlayer5Message {
        val index = values["index"]!!.toInt()
        return OpPlayer5Message(index)
    }
}