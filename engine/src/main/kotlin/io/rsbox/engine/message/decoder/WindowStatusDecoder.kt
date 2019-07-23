package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.WindowStatusMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class WindowStatusDecoder : MessageDecoder<WindowStatusMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): WindowStatusMessage {
        return WindowStatusMessage(values["mode"]!!.toInt(), values["width"]!!.toInt(), values["height"]!!.toInt())
    }
}