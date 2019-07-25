package io.rsbox.api.entity

import io.rsbox.api.AttributeMap
import io.rsbox.api.QueueTask
import io.rsbox.api.TaskPriority
import kotlinx.coroutines.CoroutineScope

/**
 * @author Kyle Escobar
 */

interface Pawn : Entity {
    /**
     * Returns the pawn ATTR map
     */
    fun getAttributes(): AttributeMap

    /**
     * Create a queue on the [Pawn]
     * @param priority Task priority
     * @param logic [Unit] lamda logic
     */
    fun queue(priority: TaskPriority = TaskPriority.STANDARD, logic: suspend QueueTask.(CoroutineScope) -> Unit)

    /**
     * Terminates any on-going [RSQueueTask]s that are being executed by this [RSPawn].
     */
    fun interruptQueues()
}