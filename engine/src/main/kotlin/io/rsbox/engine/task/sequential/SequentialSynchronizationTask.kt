package io.rsbox.engine.task.sequential

import io.rsbox.engine.model.World
import io.rsbox.engine.service.GameService
import io.rsbox.engine.sync.task.*
import io.rsbox.engine.task.GameTask

/**
 * A [GameTask] that is responsible for sending [io.rsbox.engine.model.entity.Pawn]
 * data to [io.rsbox.engine.model.entity.Pawn]s.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class SequentialSynchronizationTask : GameTask {

    override fun execute(world: World, service: GameService) {
        val worldPlayers = world.players
        val worldNpcs = world.npcs
        val rawNpcs = world.npcs.entries
        val npcSync = NpcSynchronizationTask(rawNpcs)

        worldPlayers.forEach { p ->
            PlayerPreSynchronizationTask.run(p)
        }

        for (n in worldNpcs.entries) {
            if (n != null) {
                NpcPreSynchronizationTask.run(n)
            }
        }

        worldPlayers.forEach { p ->
            /*
             * Non-human [io.rsbox.engine.model.entity.Player]s do not need this
             * to send any synchronization data to their game-client as they do
             * not have one.
             */
            if (p.entityType.isHumanControlled && p.initiated) {
                PlayerSynchronizationTask.run(p)
            }
        }

        worldPlayers.forEach { p ->
            /*
             * Non-human [io.rsbox.engine.model.entity.Player]s do not need this
             * to send any synchronization data to their game-client as they do
             * not have one.
             */
            if (p.entityType.isHumanControlled && p.initiated) {
                npcSync.run(p)
            }
        }

        worldPlayers.forEach { p ->
            PlayerPostSynchronizationTask.run(p)
        }

        for (n in worldNpcs.entries) {
            if (n != null) {
                NpcPostSynchronizationTask.run(n)
            }
        }
    }
}