package io.rsbox.engine.sync.task

import io.rsbox.engine.model.Tile
import io.rsbox.engine.model.entity.Npc
import io.rsbox.engine.sync.SynchronizationTask

/**
 * @author Tom <rspsmods@gmail.com>
 */
object NpcPostSynchronizationTask : SynchronizationTask<Npc> {

    override fun run(pawn: Npc) {
        val oldTile = pawn.lastTile
        val moved = oldTile == null || !oldTile.sameAs(pawn.tile)

        if (moved) {
            pawn.lastTile = Tile(pawn.tile)
        }
        pawn.moved = false
        pawn.steps = null
        pawn.blockBuffer.clean()
    }
}