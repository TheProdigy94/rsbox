package io.rsbox.engine.sync.task

import io.rsbox.engine.model.entity.RSNpc
import io.rsbox.engine.sync.SynchronizationTask

/**
 * @author Tom <rspsmods@gmail.com>
 */
object NpcPreSynchronizationTask : SynchronizationTask<RSNpc> {

    override fun run(pawn: RSNpc) {
        pawn.movementQueue.cycle()
    }
}