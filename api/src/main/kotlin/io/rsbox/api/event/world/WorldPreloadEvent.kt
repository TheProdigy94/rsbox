package io.rsbox.api.event.world

import io.rsbox.api.World
import io.rsbox.api.event.HandlerList

/**
 * @author Kyle Escobar
 */

class WorldPreloadEvent(world: World) : WorldEvent(world) {
    private val handlers = HandlerList()

    override fun getHandlers(): HandlerList {
        return handlers
    }
}