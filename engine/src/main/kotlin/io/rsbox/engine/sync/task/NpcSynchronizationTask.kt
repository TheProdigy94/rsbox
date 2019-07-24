package io.rsbox.engine.sync.task

import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.entity.RSNpc
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.sync.SynchronizationSegment
import io.rsbox.engine.sync.SynchronizationTask
import io.rsbox.engine.sync.segment.*
import io.rsbox.net.packet.GamePacketBuilder
import io.rsbox.net.packet.PacketType

/**
 * @author Tom <rspsmods@gmail.com>
 */
class NpcSynchronizationTask(private val worldNpcs: Array<RSNpc?>) : SynchronizationTask<RSPlayer> {

    override fun run(pawn: RSPlayer) {
        val largeScene = pawn.hasLargeViewport()

        val opcode = if (!largeScene) pawn.world.npcUpdateBlocks.updateOpcode
                        else pawn.world.npcUpdateBlocks.largeSceneUpdateOpcode

        val buf = GamePacketBuilder(opcode, PacketType.VARIABLE_SHORT)
        val maskBuf = GamePacketBuilder()

        buf.switchToBitAccess()

        val segments = getSegments(pawn)
        segments.forEach { segment ->
            segment.encode(if (segment is NpcUpdateBlockSegment) maskBuf else buf)
        }

        if (maskBuf.byteBuf.writerIndex() > 0) {
            buf.putBits(15, 0x7FFF)
        }

        buf.switchToByteAccess()

        buf.putBytes(maskBuf.byteBuf)
        pawn.write(buf.toGamePacket())
    }

    private fun getSegments(player: RSPlayer): List<SynchronizationSegment> {
        val segments = mutableListOf<SynchronizationSegment>()

        val localNpcs = player.localNpcs
        val iterator = localNpcs.iterator()

        segments.add(NpcCountSegment(localNpcs.size))
        while (iterator.hasNext()) {
            val npc = iterator.next()
            if (shouldRemove(player, npc)) {
                segments.add(RemoveLocalNpcSegment())
                iterator.remove()
                continue
            }
            npc.setActive(true)

            val requiresBlockUpdate = npc.blockBuffer.isDirty()

            if (npc.moved) {
                segments.add(NpcSkipSegment(skip = false))
                segments.add(NpcTeleportSegment())
            } else if (npc.steps != null) {
                segments.add(NpcSkipSegment(skip = false))
                segments.add(NpcWalkSegment(npc.steps!!.walkDirection!!.npcWalkValue,
                        npc.steps!!.runDirection?.npcWalkValue ?: -1, requiresBlockUpdate))
                if (requiresBlockUpdate) {
                    segments.add(NpcUpdateBlockSegment(npc, false))
                }
            } else if (requiresBlockUpdate) {
                segments.add(NpcSkipSegment(skip = false))
                segments.add(NpcNoMovementSegment())
                segments.add(NpcUpdateBlockSegment(npc, false))
            } else {
                segments.add(NpcSkipSegment(skip = true))
            }
        }

        var added = 0

        for (npc in worldNpcs) {
            if (added >= MAX_NPC_ADDITIONS_PER_CYCLE || player.localNpcs.size >= MAX_LOCAL_NPCS) {
                break
            }

            if (npc == null || !shouldAdd(player, npc) || player.localNpcs.contains(npc)) {
                continue
            }

            val requiresBlockUpdate = npc.blockBuffer.isDirty()
            segments.add(AddLocalNpcSegment(player, npc, requiresBlockUpdate, player.hasLargeViewport()))
            if (requiresBlockUpdate) {
                segments.add(NpcUpdateBlockSegment(npc, true))
            }

            added++
            player.localNpcs.add(npc)
        }

        return segments
    }

    private fun shouldRemove(player: RSPlayer, npc: RSNpc): Boolean = !npc.isSpawned() || npc.invisible || !isWithinView(player, npc.tile)

    private fun shouldAdd(player: RSPlayer, npc: RSNpc): Boolean = npc.isSpawned() && !npc.invisible && isWithinView(player, npc.tile) && (npc.owner == null || npc.owner == player)

    private fun isWithinView(player: RSPlayer, tile: RSTile): Boolean = tile.isWithinRadius(player.tile, if (player.hasLargeViewport()) RSPlayer.LARGE_VIEW_DISTANCE else RSPlayer.NORMAL_VIEW_DISTANCE)

    companion object {
        private const val MAX_LOCAL_NPCS = 255
        private const val MAX_NPC_ADDITIONS_PER_CYCLE = 40
    }
}
