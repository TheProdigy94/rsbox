package io.rsbox.engine.task

import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.service.GameService

/**
 * A [GameTask] responsible for handling entity removal from the [RSWorld] when
 * appropriate.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class WorldRemoveTask : GameTask {

    override fun execute(world: RSWorld, service: GameService) {
        for (i in 0 until world.npcs.capacity) {
            val npc = world.npcs[i] ?: continue
            if (npc.owner?.isOnline == false) {
                world.remove(npc)
            }
        }
    }
}