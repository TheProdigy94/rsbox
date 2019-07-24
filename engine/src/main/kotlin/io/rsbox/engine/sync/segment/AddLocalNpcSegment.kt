package io.rsbox.engine.sync.segment

import io.rsbox.engine.model.Direction
import io.rsbox.engine.model.entity.RSNpc
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.sync.SynchronizationSegment
import io.rsbox.net.packet.GamePacketBuilder

/**
 * @author Tom <rspsmods@gmail.com>
 */
class AddLocalNpcSegment(val player: RSPlayer, val npc: RSNpc, private val requiresBlockUpdate: Boolean,
                         private val largeScene: Boolean) : SynchronizationSegment {

    override fun encode(buf: GamePacketBuilder) {
        var dx = npc.tile.x - player.tile.x
        var dz = npc.tile.z - player.tile.z
        if (!largeScene) {
            if (dx < RSPlayer.NORMAL_VIEW_DISTANCE) {
                dx += 32
            }
            if (dz < RSPlayer.NORMAL_VIEW_DISTANCE) {
                dz += 32
            }
        } else {
            if (dx < RSPlayer.LARGE_VIEW_DISTANCE) {
                dx += 256
            }
            if (dz < RSPlayer.LARGE_VIEW_DISTANCE) {
                dz += 256
            }
        }

        val id = if (npc.getTransmogId() != -1) npc.getTransmogId() else npc.id
        val facing = if (npc.lastFacingDirection != Direction.NONE) npc.lastFacingDirection else Direction.SOUTH

        buf.putBits(15, npc.index)
        buf.putBits(if (largeScene) 8 else 5, dx)
        buf.putBits(1, if (requiresBlockUpdate) 1 else 0)
        buf.putBits(3, facing.npcWalkValue)
        buf.putBits(1, if (requiresBlockUpdate) 1 else 0)
        buf.putBits(if (largeScene) 8 else 5, dz)
        buf.putBits(14, id)
    }
}