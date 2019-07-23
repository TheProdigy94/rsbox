package io.rsbox.engine.task

import io.rsbox.engine.model.World
import io.rsbox.engine.service.GameService

/**
 * A [GameTask] is anything that can be scheduled to be executed on the
 * game-thread.
 *
 * @author Tom <rspsmods@gmail.com>
 */
interface GameTask {

    /**
     * Executes the [GameTask] logic.
     */
    fun execute(world: World, service: GameService)
}