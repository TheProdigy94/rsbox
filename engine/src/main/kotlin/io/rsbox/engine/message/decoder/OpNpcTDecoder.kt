package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpNpcTMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpNpcTDecoder : MessageDecoder<OpNpcTMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpNpcTMessage {
        val npcIndex = values["npc_index"]!!.toInt()
        val componentHash = values["component_hash"]!!.toInt()
        val componentSlot = values["component_slot"]!!.toInt()
        val movementType = values["movement_type"]!!.toInt()
        return OpNpcTMessage(npcIndex, componentHash, componentSlot, movementType)
    }
}