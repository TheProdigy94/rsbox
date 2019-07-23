package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.EventAppletFocusMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class EventAppletFocusDecoder : MessageDecoder<EventAppletFocusMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): EventAppletFocusMessage {
        val state = values["state"]!!.toInt()
        return EventAppletFocusMessage(state)
    }
}