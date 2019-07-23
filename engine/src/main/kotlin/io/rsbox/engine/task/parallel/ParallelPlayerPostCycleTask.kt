package io.rsbox.engine.task.parallel

import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.service.GameService
import io.rsbox.engine.task.GameTask
import io.rsbox.util.concurrency.PhasedTask
import java.util.concurrent.ExecutorService
import java.util.concurrent.Phaser

/**
 * A [GameTask] responsible for executing [io.rsbox.engine.model.entity.Pawn]
 * "post" cycle logic, in parallel. Post cycle means that the this task
 * will be handled near the end of the cycle, after the synchronization
 * tasks.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class ParallelPlayerPostCycleTask(private val executor: ExecutorService) : GameTask {

    private val phaser = Phaser(1)

    override fun execute(world: RSWorld, service: GameService) {
        val worldPlayers = world.players
        val playerCount = worldPlayers.count()

        phaser.bulkRegister(playerCount)
        worldPlayers.forEach { p ->
            executor.execute {
                PhasedTask.run(phaser) {
                    p.postCycle()
                }
            }
        }
        phaser.arriveAndAwaitAdvance()
    }
}