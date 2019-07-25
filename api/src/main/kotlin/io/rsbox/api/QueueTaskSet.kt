package io.rsbox.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

/**
 * @author Kyle Escobar
 */

interface QueueTaskSet {
    fun queue(ctx: Any, dispatcher: CoroutineDispatcher, priority: TaskPriority, block: suspend QueueTask.(CoroutineScope) -> Unit)

    fun submitReturnValue(value: Any)

    fun terminateTasks()
}