package io.rsbox.api.event.world

import io.rsbox.api.World
import io.rsbox.api.event.Event

/**
 * @author Kyle Escobar
 */

abstract class WorldEvent(private val world: World) : Event() {
    fun getWorld(): World {
        return world
    }
}