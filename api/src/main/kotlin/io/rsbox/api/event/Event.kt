package io.rsbox.api.event

/**
 * @author Kyle Escobar
 */

abstract class Event(val async: Boolean = false) {

    lateinit var args: Array<out Any?>

    fun getEventName(): String {
        return this.javaClass.simpleName
    }

    fun getEventClass(): Class<out Event> {
        return this.javaClass
    }

    open fun init(vararg objArgs: Any?) {
        @Suppress("UNCHECKED_CAST")
        args = objArgs[0] as Array<Any>
    }
}