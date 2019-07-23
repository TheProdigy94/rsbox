package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpLocUMessage

/**
 * @author Triston Plummer ("Dread")
 *
 * Decodes an incoming item on object message
 */
class OpLocUDecoder : MessageDecoder<OpLocUMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpLocUMessage {
        val x = values["pos_x"]!!.toInt()
        val z = values["pos_z"]!!.toInt()
        val slot = values["slot"]!!.toInt()
        val item = values["item"]!!.toInt()
        val obj = values["obj"]!!.toInt()
        val movementType = values["movement_type"]!!.toInt()

        return OpLocUMessage(x = x, z = z, slot = slot, item = item, obj = obj, movementType = movementType)
    }
}