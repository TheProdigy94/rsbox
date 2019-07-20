package io.rsbox.api.event

/**
 * @author Kyle Escobar
 */

interface EventExecutor {
    fun execute(listener: Listener, event: Event)
}