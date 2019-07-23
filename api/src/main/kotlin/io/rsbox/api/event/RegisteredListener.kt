package io.rsbox.api.event

import io.rsbox.api.plugin.RSBoxPlugin
import java.lang.reflect.Method

/**
 * @author Kyle Escobar
 */

class RegisteredListener(val listener: EventListener, val plugin: RSBoxPlugin) {
    private val handlers = hashMapOf<Class<*>, Method>()

    init {
        val clazz = listener.javaClass
        val methods: Array<Method> = clazz.methods

        methods.forEach { method ->
            method.annotations.forEach { annotation ->
                if(annotation is EventHandler) {
                    if(handlers.containsKey(method.parameters[0].type)) {
                        throw EventException("Event listener ${listener.javaClass.simpleName} was not registered because duplicate event handlers exist. Please create a separate listener class or combine the methods.")
                    }
                    handlers.put(method.parameters[0].type, method)
                }
            }
        }
    }

    fun getHandlers(): HashMap<Class<*>, Method> {
        return this.handlers
    }
}