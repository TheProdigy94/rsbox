package io.rsbox.engine.sync.task

import io.rsbox.engine.model.entity.Npc
import io.rsbox.engine.sync.SynchronizationTask

/**
 * @author Tom <rspsmods@gmail.com>
 */
object NpcPreSynchronizationTask : SynchronizationTask<Npc> {

    override fun run(pawn: Npc) {
        pawn.movementQueue.cycle()
    }
}