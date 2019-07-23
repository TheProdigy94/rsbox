package io.rsbox.engine.message.decoder

import io.rsbox.engine.message.MessageDecoder
import io.rsbox.engine.message.impl.ClientCheatMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ClientCheatDecoder : MessageDecoder<ClientCheatMessage>() {

    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): ClientCheatMessage {
        return ClientCheatMessage(stringValues["command"]!!)
    }
}
