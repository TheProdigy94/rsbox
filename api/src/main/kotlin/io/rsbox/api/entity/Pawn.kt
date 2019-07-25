package io.rsbox.api.entity

import io.rsbox.api.AttributeMap
import io.rsbox.api.LockState
import io.rsbox.api.QueueTask
import io.rsbox.api.TaskPriority
import io.rsbox.api.item.Item
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

    fun lock()

    fun unlock()

    fun isLocked(): Boolean

    /**
     * Gets the arguments as [String] that was passed in the last command.
     */
    fun getCommandArgs(): Array<String>

    /**
     * Gets the interface slot of the current interacting interface
     */
    fun getInteractingSlot(): Int

    /**
     * Gets the [Item] from interating interface
     */
    fun getInteractingItem(): Item

    fun getInteractingItemId(): Int

    fun getInteractingItemSlot(): Int

    fun getInteractingOption(): Int

    fun getInteractingGameObj(): GameObject

    fun getInteractingNpc(): Npc

    fun getInteractingPlayer() : Player

    fun getLockState(): LockState
}