package io.rsbox.engine.task.parallel

import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.service.GameService
import io.rsbox.engine.task.GameTask
import io.rsbox.util.concurrency.PhasedTask
import java.util.concurrent.ExecutorService
import java.util.concurrent.Phaser

/**
 * A [GameTask] responsible for executing [io.rsbox.engine.model.entity.Npc]
 * cycle logic, in parallel.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class ParallelNpcCycleTask(private val executor: ExecutorService) : GameTask {

    private val phaser = Phaser(1)

    override fun execute(world: RSWorld, service: GameService) {
        val worldNpcs = world.npcs
        val npcCount = worldNpcs.count()

        phaser.bulkRegister(npcCount)
        worldNpcs.forEach { n ->
            executor.execute {
                PhasedTask.run(phaser) {
                    n.cycle()
                }
            }
        }
        phaser.arriveAndAwaitAdvance()
    }
}