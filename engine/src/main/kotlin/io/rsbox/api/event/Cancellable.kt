package io.rsbox.api.event

/**
 * Implemented on events that can have their actions canceled by the logic.
 * @author Kyle Escobar
 */
interface Cancellable {
    /**
     * Gets whether or not an event's actions should be cancelled.
     * @return [Boolean]
     */
    fun isCancelled(): Boolean

    /**
     * Sets the cancellation of an event.
     * @param cancel [Boolean] -> true = stop actions, false = continue actions.
     */
    fun setCancelled(cancel: Boolean)
}