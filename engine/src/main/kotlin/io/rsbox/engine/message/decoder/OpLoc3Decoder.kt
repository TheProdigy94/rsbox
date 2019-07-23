package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpLoc3Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpLoc3Decoder : MessageDecoder<OpLoc3Message>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpLoc3Message {
        val id = values["id"]!!.toInt()
        val x = values["x"]!!.toInt()
        val z = values["z"]!!.toInt()
        val movementType = values["movement_type"]!!.toInt()
        return OpLoc3Message(id, x, z, movementType)
    }
}