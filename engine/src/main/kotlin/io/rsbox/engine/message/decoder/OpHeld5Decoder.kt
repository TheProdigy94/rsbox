package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpHeld5Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpHeld5Decoder : MessageDecoder<OpHeld5Message>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpHeld5Message {
        val componentHash = values["component_hash"]!!.toInt()
        val slot = values["slot"]!!.toInt()
        val item = values["item"]!!.toInt()
        return OpHeld5Message(componentHash, slot, item)
    }
}