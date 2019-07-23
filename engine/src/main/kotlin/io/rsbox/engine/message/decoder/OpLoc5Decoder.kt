package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpLoc5Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpLoc5Decoder : MessageDecoder<OpLoc5Message>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpLoc5Message {
        val id = values["id"]!!.toInt()
        val x = values["x"]!!.toInt()
        val z = values["z"]!!.toInt()
        val movementType = values["movement_type"]!!.toInt()
        return OpLoc5Message(id, x, z, movementType)
    }
}