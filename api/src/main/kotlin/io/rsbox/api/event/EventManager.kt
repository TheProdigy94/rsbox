package io.rsbox.api.event

import io.rsbox.api.event.login.PlayerLoginEvent
import io.rsbox.api.plugin.PluginManager

/**
 * @author Kyle Escobar
 */

object EventManager {
    private val events = arrayListOf<Event>()

    init {
        register(PlayerLoginEvent::class.java)
    }

    fun fireEvent(eventClass: Class<out Event>, vararg args: Any?): Boolean {
        events.forEach { event ->
            if(event.getEventClass() == eventClass) {

                // Add the needed args to the event fired
                event.init(args)
                this.invokePluginListeners(event)

                if(event is Cancellable) {
                    return !event.isCancelled()
                }
                return true
            }
        }
        throw EventException("Event fired does not have initialized event instance.")
    }

    private fun invokePluginListeners(event: Event) {
        PluginManager.plugins.forEach { plugin ->
            plugin.getEventListeners().forEach { listener ->
                val handler = listener.getHandlers().get(event.javaClass)
                if(handler != null) {
                    listener.listener.javaClass.getMethod(handler.name, *handler.parameterTypes).invoke(listener.listener, event)
                }
            }
        }
    }

    fun register(clazz: Class<out Event>) {
        val event = clazz.newInstance()
        events.add(event)
    }
}