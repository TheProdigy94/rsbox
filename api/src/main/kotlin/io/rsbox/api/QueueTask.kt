package io.rsbox.api

import io.rsbox.api.entity.Npc
import io.rsbox.api.entity.Pawn
import io.rsbox.api.entity.Player

/**
 * Interface which represents methods to be called on queues.
 */
interface QueueTask {

    val pawn: Pawn

    val player: Player

    val npc: Npc

    /**
     * Close appearance interface
     */
    val closeAppearance: QueueTask.() -> Unit

    /**
     * Opens the appearance selection
     */
    suspend fun selectAppearance(): Appearance?
}