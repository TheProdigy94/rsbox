package io.rsbox.engine.event

import io.rsbox.api.World
import io.rsbox.api.event.EventManager
import io.rsbox.api.event.world.WorldPreloadEvent

/**
 * @author Kyle Escobar
 */

object RSEventFactory {

    /**
     * Event : WorldLoadEvent
     * Called right after the world is initialized.
     */
    fun callWorldPreloadEvent(world: World):  WorldPreloadEvent {
        val event = WorldPreloadEvent(world)
        EventManager.callEvent(event)
        return event
    }
}