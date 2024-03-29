package io.rsbox.engine.task

import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.service.GameService

/**
 * A [GameTask] responsible for handling all incoming
 * [io.rsbox.engine.message.Message]s, sequentially.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class MessageHandlerTask : GameTask {

    override fun execute(world: RSWorld, service: GameService) {
        world.players.forEach { p ->
            val start = System.currentTimeMillis()
            p.handleMessages()
            /*
             * Log the time it takes for the task to handle all the player's
             * incoming messages.
             */
            val time = System.currentTimeMillis() - start
            service.playerTimes.merge(p.username, time) { _, oldTime -> oldTime + time }
        }
    }
}