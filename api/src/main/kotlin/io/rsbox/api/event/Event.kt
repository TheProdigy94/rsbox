package io.rsbox.api.event

/**
 * @author Kyle Escobar
 */

abstract class Event(val async: Boolean = false) {
    fun getEventName(): String {
        return this.javaClass.simpleName
    }

    fun getEventClass(): Class<out Event> {
        return this.javaClass
    }
}