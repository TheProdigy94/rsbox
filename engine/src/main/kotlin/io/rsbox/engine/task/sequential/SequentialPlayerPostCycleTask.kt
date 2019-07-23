package io.rsbox.engine.task.sequential

import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.service.GameService
import io.rsbox.engine.task.GameTask

/**
 * A [GameTask] responsible for executing [io.rsbox.engine.model.entity.Pawn]
 * "post" cycle logic, sequentially. Post cycle means that the this task
 * will be handled near the end of the cycle, after the synchronization
 * tasks.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class SequentialPlayerPostCycleTask : GameTask {

    override fun execute(world: RSWorld, service: GameService) {
        world.players.forEach { p ->
            p.postCycle()
        }
    }
}