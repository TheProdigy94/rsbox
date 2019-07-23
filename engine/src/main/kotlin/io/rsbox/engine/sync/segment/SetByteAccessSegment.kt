package io.rsbox.engine.sync.segment

import io.rsbox.engine.sync.SynchronizationSegment
import io.rsbox.net.packet.GamePacketBuilder

/**
 * @author Tom <rspsmods@gmail.com>
 */
class SetByteAccessSegment : SynchronizationSegment {

    override fun encode(buf: GamePacketBuilder) {
        buf.switchToByteAccess()
    }
}