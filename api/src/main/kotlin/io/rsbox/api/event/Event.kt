package io.rsbox.api.event

/**
 * @author Kyle Escobar
 * // TODO Document this class
 */

abstract class Event(val async: Boolean = false) {
    private lateinit var name: String

    fun getEventName(): String {
        if(name == null) {
            name = javaClass.simpleName
        }
        return name
    }

    abstract fun getHandlers(): HandlerList

    fun isAsync(): Boolean {
        return async
    }

    enum class Result {
        /**
         * Deny the event. Depending on the event, the action indicated by the
         * event will either not take place or will be reverted. Some actions
         * may not be denied.
         */
        DENY,

        /**
         * Neigher deny nor allow the event. The server will proceed with its
         * normal handling.
         */
        NORMAL,

        /**
         * Allow / Force the event. The action indicated by the event will
         * take place if possible, even if the server would not normally allow
         * the action. Some actions may not be allowed.
         */
        ALLOW;
    }
}