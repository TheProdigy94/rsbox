package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpHeldDMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpHeldDDecoder : MessageDecoder<OpHeldDMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpHeldDMessage {
        val srcSlot = values["src_slot"]!!.toInt()
        val dstSlot = values["dst_slot"]!!.toInt()
        val componentHash = values["component_hash"]!!.toInt()
        val insertMode = values["insert_mode"]!!.toInt() == 1
        return OpHeldDMessage(srcSlot, dstSlot, componentHash, insertMode)
    }
}