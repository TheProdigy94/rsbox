package io.rsbox.engine.sync.segment

import io.rsbox.engine.sync.SynchronizationSegment
import io.rsbox.net.packet.GamePacketBuilder

/**
 * @author Tom <rspsmods@gmail.com>
 */
class NpcSkipSegment(private val skip: Boolean) : SynchronizationSegment {

    override fun encode(buf: GamePacketBuilder) {
        buf.putBits(1, if (skip) 0 else 1)
    }
}