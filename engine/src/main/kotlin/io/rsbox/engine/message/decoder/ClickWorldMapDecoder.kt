package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.ClickWorldMapMessage

/**
 * @author HolyRSPS <dagreenrs@gmail.com>
 */
class ClickWorldMapDecoder : MessageDecoder<ClickWorldMapMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): ClickWorldMapMessage {
        val data = values["data"]!!.toInt()
        return ClickWorldMapMessage(data)
    }
}