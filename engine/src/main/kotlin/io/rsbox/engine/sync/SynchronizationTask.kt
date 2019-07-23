package io.rsbox.engine.sync

import io.rsbox.engine.model.entity.Pawn

/**
 * A task in any pawn synchronization process.
 *
 * @author Tom <rspsmods@gmail.com>
 */
interface SynchronizationTask<T : Pawn> {

    fun run(pawn: T)
}