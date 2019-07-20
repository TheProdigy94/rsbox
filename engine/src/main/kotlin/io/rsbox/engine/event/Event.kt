package io.rsbox.engine.event

/**
 * @author Kyle Escobar
 */

open class Event<T> {
    var handlers = listOf<(T) -> Unit>()

    infix fun on(handler: (T) -> Unit) {
        handlers = handlers + handler
    }

    fun emit(event: T) {
        for(subscriber in handlers) {
            subscriber(event)
        }
    }
}