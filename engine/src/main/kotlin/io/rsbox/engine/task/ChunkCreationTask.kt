package io.rsbox.engine.task

import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSPawn
import io.rsbox.engine.service.GameService

/**
 * A [GameTask] responsible for creating any non-existent [io.rsbox.engine.model.region.Chunk]
 * that players are standing on as well as registering and de-registering the
 * player from the respective [io.rsbox.engine.model.region.Chunk]s.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class ChunkCreationTask : GameTask {

    override fun execute(world: RSWorld, service: GameService) {
        world.players.forEach { p ->
            p.changeChunks(world, createChunkIfNeeded = true)
        }

        world.npcs.forEach { npc ->
            if (npc.isActive()) {
                npc.changeChunks(world, createChunkIfNeeded = CREATE_CHUNK_FOR_NPC)
            }
        }
    }

    private fun <T : RSPawn> T.changeChunks(world: RSWorld, createChunkIfNeeded: Boolean) {
        val lastTile = lastChunkTile
        val sameTile = lastTile?.sameAs(tile) ?: false

        if (sameTile) {
            return
        }

        if (lastTile != null) {
            world.chunks.get(lastTile)?.removeEntity(world, this, lastTile)
        }

        world.chunks.get(tile as RSTile, createIfNeeded = createChunkIfNeeded)?.addEntity(world, this, tile as RSTile)
        lastChunkTile = RSTile(tile as RSTile)
    }

    companion object {
        /**
         * Flag that specifies if [io.rsbox.engine.model.region.Chunk] should be
         * created if an npc is on it and it doesn't already exist.
         */
        private const val CREATE_CHUNK_FOR_NPC = false
    }
}