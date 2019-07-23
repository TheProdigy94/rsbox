package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpPlayer6Message

/**
 * @author Triston Plummer ("Dread")
 */
class OpPlayer6Decoder : MessageDecoder<OpPlayer6Message>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpPlayer6Message {
        val index = values["index"]!!.toInt()
        return OpPlayer6Message(index)
    }
}