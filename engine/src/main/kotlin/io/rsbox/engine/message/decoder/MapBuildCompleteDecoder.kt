package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.MapBuildCompleteMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class MapBuildCompleteDecoder : MessageDecoder<MapBuildCompleteMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): MapBuildCompleteMessage = MapBuildCompleteMessage()
}