package io.rsbox.engine.task.sequential

import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.service.GameService
import io.rsbox.engine.task.GameTask

/**
 * A [GameTask] responsible for executing [io.rsbox.engine.model.entity.Player]
 * cycle logic, sequentially.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class SequentialPlayerCycleTask : GameTask {

    override fun execute(world: RSWorld, service: GameService) {
        world.players.forEach { p ->
            val start = System.currentTimeMillis()
            p.cycle()
            /*
             * Log the time it takes for task to handle the player's cycle
             * logic.
             */
            val time = System.currentTimeMillis() - start
            service.playerTimes.merge(p.username, time) { _, oldTime -> oldTime + time }
        }
    }
}