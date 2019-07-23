package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.EventCameraPositionMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class EventCameraPositionDecoder : MessageDecoder<EventCameraPositionMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): EventCameraPositionMessage {
        val mouseX = values["pitch"]!!.toInt()
        val mouseY = values["yaw"]!!.toInt()
        return EventCameraPositionMessage(mouseX, mouseY)
    }
}