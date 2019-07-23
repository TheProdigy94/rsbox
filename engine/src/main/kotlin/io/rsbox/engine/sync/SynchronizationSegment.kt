package io.rsbox.engine.sync

import io.rsbox.net.packet.GamePacketBuilder

/**
 * @author Tom <rspsmods@gmail.com>
 */
interface SynchronizationSegment {

    fun encode(buf: GamePacketBuilder)
}