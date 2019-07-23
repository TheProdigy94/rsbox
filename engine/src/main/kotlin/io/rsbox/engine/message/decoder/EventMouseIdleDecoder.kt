package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.EventMouseIdleMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class EventMouseIdleDecoder : MessageDecoder<EventMouseIdleMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): EventMouseIdleMessage {
        return EventMouseIdleMessage()
    }
}