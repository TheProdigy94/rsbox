package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpPlayer1Message

/**
 * @author Triston Plummer ("Dread")
 */
class OpPlayer1Decoder : MessageDecoder<OpPlayer1Message>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpPlayer1Message {
        val index = values["index"]!!.toInt()
        return OpPlayer1Message(index)
    }
}