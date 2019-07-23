package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.OpHeldTMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpHeldTDecoder : MessageDecoder<OpHeldTMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): OpHeldTMessage {
        val fromComponentHash = values["from_hash"]!!.toInt()
        val toComponentHash = values["to_hash"]!!.toInt()
        val spellSlot = values["spell_slot"]!!.toInt()
        val itemSlot = values["item_slot"]!!.toInt()
        val itemId = values["item_id"]!!.toInt()
        return OpHeldTMessage(fromComponentHash, toComponentHash, spellSlot, itemSlot, itemId)
    }
}