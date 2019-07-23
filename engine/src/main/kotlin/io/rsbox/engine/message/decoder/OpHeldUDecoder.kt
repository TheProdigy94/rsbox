package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpHeldUMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpHeldUDecoder : MessageDecoder<OpHeldUMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpHeldUMessage {
        val fromComponent = values["from_component"]!!.toInt()
        val fromSlot = values["from_slot"]!!.toInt()
        val fromItem = values["from_item"]!!.toInt()
        val toComponent = values["to_component"]!!.toInt()
        val toSlot = values["to_slot"]!!.toInt()
        val toItem = values["to_item"]!!.toInt()

        return OpHeldUMessage(fromComponent, fromSlot, fromItem, toComponent, toSlot, toItem)
    }
}