package io.rsbox.engine.model.queue

import io.rsbox.api.TaskPriority
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.util.*
import kotlin.coroutines.createCoroutine

/**
 * A system responsible for task coroutine logic.
 *
 * @author Tom <rspsmods@gmail.com>
 */
abstract class QueueTaskSet {

    protected val queue: LinkedList<RSQueueTask> = LinkedList()

    val size: Int get() = queue.size

    abstract fun cycle()

    fun queue(ctx: Any, dispatcher: CoroutineDispatcher, priority: TaskPriority, block: suspend RSQueueTask.(CoroutineScope) -> Unit) {
        val task = RSQueueTask(ctx, priority)
        val suspendBlock = suspend { block(task, CoroutineScope(dispatcher)) }

        task.coroutine = suspendBlock.createCoroutine(completion = task)

        if (priority == TaskPriority.STRONG) {
            terminateTasks()
        }

        queue.addFirst(task)
    }

    /**
     * In-game events sometimes must return a value to a oldplugin. An example are
     * dialogs which must return values such as input, button click, etc.
     *
     * @param value
     * The return value that the oldplugin has asked for.
     */
    fun submitReturnValue(value: Any) {
        val task = queue.peek() ?: return // Shouldn't call this method without a queued task.
        task.requestReturnValue = value
    }

    /**
     * Remove all [RSQueueTask] from our [queue], invoking each task's [RSQueueTask.terminate]
     * before-hand.
     */
    fun terminateTasks() {
        queue.forEach { it.terminate() }
        queue.clear()
    }
}
