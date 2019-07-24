package io.rsbox.engine.sync.segment

import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.sync.SynchronizationSegment
import io.rsbox.net.packet.GamePacketBuilder

/**
 * @author Tom <rspsmods@gmail.com>
 */
class AddLocalPlayerSegment(private val other: RSPlayer, private val locationSegment: PlayerLocationHashSegment?) : SynchronizationSegment {

    override fun encode(buf: GamePacketBuilder) {
        /*
         * Signal to the client that the player needs to be decoded.
         */
        buf.putBits(1, 1)

        /*
         * Signal to the client that the non-local player needs to be added
         * as a local player.
         */
        buf.putBits(2, 0)

        /*
         * Signal to the client that the player needs to have their location
         * decoded.
         */
        buf.putBits(1, if (locationSegment != null) 1 else 0)

        /*
         * Encode the player's location hash.
         */
        locationSegment?.encode(buf)

        buf.putBits(13, other.tile.x and 0x1FFF)
        buf.putBits(13, other.tile.z and 0x1FFF)
        buf.putBits(1, 1) // Requires block update
    }
}