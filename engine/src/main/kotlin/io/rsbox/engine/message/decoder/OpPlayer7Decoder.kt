package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpPlayer7Message

/**
 * @author Triston Plummer ("Dread")
 */
class OpPlayer7Decoder : MessageDecoder<OpPlayer7Message>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpPlayer7Message {
        val index = values["index"]!!.toInt()
        return OpPlayer7Message(index)
    }
}