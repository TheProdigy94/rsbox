package io.rsbox.engine.task.sequential

import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.service.GameService
import io.rsbox.engine.task.GameTask

/**
 * A [GameTask] responsible for executing [io.rsbox.engine.model.entity.RSNpc]
 * cycle logic, sequentially.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class SequentialNpcCycleTask : GameTask {

    override fun execute(world: RSWorld, service: GameService) {
        world.npcs.forEach { n ->
            n.cycle()
        }
    }
}