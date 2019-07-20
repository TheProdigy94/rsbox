package io.rsbox.api.plugin

import io.rsbox.api.event.*

/**
 * Stores relevant information for plugin listeners.
 * @author Kyle Escobar
 */
class RegisteredListener(private val listener: Listener, private val executor: EventExecutor,  private val priority: EventPriority, private val ignoreCancelled: Boolean) {
    /**
     * Gets the listener for this registration
     */
    fun getListener(): Listener {
        return listener
    }

    fun getPriority(): EventPriority {
        return priority
    }

    fun callEvent(event: Event) {
        if(event is Cancellable) {
            if((event as Cancellable).isCancelled() && isIgnoringCancelled()) {
                return
            }
        }
        executor.execute(listener,event)
    }

    fun isIgnoringCancelled(): Boolean {
        return ignoreCancelled
    }
}