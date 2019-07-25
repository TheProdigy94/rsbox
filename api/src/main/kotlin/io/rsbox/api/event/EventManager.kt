package io.rsbox.api.event

import io.rsbox.api.plugin.PluginManager

/**
 * @author Kyle Escobar
 */

object EventManager {
    fun fireEvent(event: Event): Boolean {
        invokePluginListeners(event)
        if(event is Cancellable) {
            if(event.isCancelled()) {
                return false
            }
        }
        return true
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
}