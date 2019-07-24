package io.rsbox.engine.sync.task

import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.entity.RSNpc
import io.rsbox.engine.sync.SynchronizationTask

/**
 * @author Tom <rspsmods@gmail.com>
 */
object NpcPostSynchronizationTask : SynchronizationTask<RSNpc> {

    override fun run(pawn: RSNpc) {
        val oldTile = pawn.lastTile
        val moved = oldTile == null || !oldTile.sameAs(pawn.tile)

        if (moved) {
            pawn.lastTile = RSTile(pawn.tile as RSTile)
        }
        pawn.moved = false
        pawn.steps = null
        pawn.blockBuffer.clean()
    }
}