package io.rsbox.engine.sync.segment

import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.sync.SynchronizationSegment
import io.rsbox.net.packet.GamePacketBuilder

/**
 * @author Tom <rspsmods@gmail.com>
 */
class PlayerTeleportSegment(private val other: RSPlayer, private val encodeUpdateBlocks: Boolean) : SynchronizationSegment {

    override fun encode(buf: GamePacketBuilder) {
        /*
         * Signal to the client that [other] needs to be decoded.
         */
        buf.putBits(1, 1)
        /*
         * Does [other] have pending [io.rsbox.api.UpdateBlockType]s?
         */
        buf.putBit(encodeUpdateBlocks)
        /*
         * Signal to the client that [other] has been moved without actual
         * walking being involved.
         */
        buf.putBits(2, 3)

        /*
         * The difference from [other]'s last tile as far as [player]'s client is
         * concerned.
         */
        val diffX = other.tile.x - (other.lastTile?.x ?: 0)
        val diffZ = other.tile.z - (other.lastTile?.z ?: 0)
        val diffH = other.tile.height - (other.lastTile?.height ?: 0)

        /*
         * If the move is within a short radius, we want to save some bandwidth.
         */
        if (Math.abs(diffX) <= RSPlayer.NORMAL_VIEW_DISTANCE && Math.abs(diffZ) <= RSPlayer.NORMAL_VIEW_DISTANCE) {
            /*
             * Signal to the client that the difference in tiles are within
             * viewing distance.
             */
            buf.putBits(1, 0)
            /*
             * Write the difference in tiles.
             */
            buf.putBits(2, diffH and 0x3)
            buf.putBits(5, diffX and 0x1F)
            buf.putBits(5, diffZ and 0x1F)
        } else {
            /*
             * Signal to the client that the difference in tiles are not within
             * viewing distance.
             */
            buf.putBits(1, 1)
            /*
             * Write the difference in tiles.
             */
            buf.putBits(2, diffH and 0x3)
            buf.putBits(14, diffX and 0x3FFF)
            buf.putBits(14, diffZ and 0x3FFF)
        }
    }
}