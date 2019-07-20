package io.rsbox.api.event.world

import io.rsbox.api.World
import io.rsbox.api.event.Cancellable

/**
 * @author Kyle Escobar
 */

data class WorldPreloadEvent(val world: World) : WorldEvent(world), Cancellable {
    private var cancel = false

    override fun isCancelled(): Boolean {
        return this.cancel
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }
}