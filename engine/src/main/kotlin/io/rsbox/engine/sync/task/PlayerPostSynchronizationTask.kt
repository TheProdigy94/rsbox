package io.rsbox.engine.sync.task

import io.rsbox.engine.model.Tile
import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.service.GameService
import io.rsbox.engine.sync.SynchronizationTask
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

/**
 * @author Tom <rspsmods@gmail.com>
 */
object PlayerPostSynchronizationTask : SynchronizationTask<Player> {

    override fun run(pawn: Player) {
        val oldTile = pawn.lastTile
        val moved = oldTile == null || !oldTile.sameAs(pawn.tile)
        val changedHeight = oldTile?.height != pawn.tile.height

        if (moved) {
            pawn.lastTile = Tile(pawn.tile)
        }
        pawn.moved = false
        pawn.steps = null
        pawn.blockBuffer.clean()

        if (moved) {
            val oldChunk = if (oldTile != null) pawn.world.chunks.get(oldTile.chunkCoords, createIfNeeded = false) else null
            val newChunk = pawn.world.chunks.get(pawn.tile.chunkCoords, createIfNeeded = false)
            if (newChunk != null && (oldChunk != newChunk || changedHeight)) {
                pawn.world.getService(GameService::class.java)?.let { service ->
                    val newSurroundings = newChunk.coords.getSurroundingCoords()
                    if (!changedHeight) {
                        val oldSurroundings = oldChunk?.coords?.getSurroundingCoords() ?: ObjectOpenHashSet()
                        newSurroundings.removeAll(oldSurroundings)
                    }

                    newSurroundings.forEach { coords ->
                        val chunk = pawn.world.chunks.get(coords, createIfNeeded = false) ?: return@forEach
                        chunk.sendUpdates(pawn, service)
                    }
                }
                if (!changedHeight) {
                    if (oldChunk != null) {
                        pawn.world.plugins.executeChunkExit(pawn, oldChunk.hashCode())
                    }
                    pawn.world.plugins.executeChunkEnter(pawn, newChunk.hashCode())
                }
            }
        }
    }
}