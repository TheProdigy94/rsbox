package io.rsbox.engine.model.queue.impl

import io.rsbox.engine.game.Game
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.model.queue.QueueTaskSet
import io.rsbox.api.TaskPriority
import kotlin.coroutines.resume

/**
 * A [QueueTaskSet] implementation for [io.rsbox.engine.model.entity.RSPawn]s.
 * Each [io.rsbox.engine.model.queue.RSQueueTask] is handled one at a time.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class PawnQueueTaskSet : QueueTaskSet() {

    override fun cycle() {
        while (true) {
            val task = queue.peekFirst() ?: break

            if (task.priority == TaskPriority.STANDARD && task.ctx is RSPlayer && task.ctx.hasMenuOpen()) {
                break
            }

            if (!task.invoked) {
                task.invoked = true
                task.coroutine.resume(Unit)
            }

            task.cycle()

            if (!task.suspended()) {
                /*
                 * Task is no longer in a suspended state, which means its job is
                 * complete.
                 */
                queue.remove(task)
                /*
                 * Since this task is complete, let's handle any upcoming
                 * task now instead of waiting until next cycle.
                 */
                continue
            }
            break
        }
    }

    private fun RSPlayer.hasMenuOpen(): Boolean = Game.openMenuCheck(this)
}