package io.rsbox.api.event.world

import io.rsbox.api.World

/**
 * @author Kyle Escobar
 */

abstract class WorldEvent(private val world: World) {
    fun getWorld(): World {
        return world
    }
}