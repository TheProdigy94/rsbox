package io.rsbox.engine.sync

import io.rsbox.engine.model.entity.RSPawn

/**
 * A task in any pawn synchronization process.
 *
 * @author Tom <rspsmods@gmail.com>
 */
interface SynchronizationTask<T : RSPawn> {

    fun run(pawn: T)
}