package io.rsbox.api.event

/**
 * @author Kyle Escobar
 */

interface Cancellable {
    fun isCancelled(): Boolean

    fun setCancelled(cancel: Boolean)
}