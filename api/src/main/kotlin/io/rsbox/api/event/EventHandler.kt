package io.rsbox.api.event

/**
 * @author Kyle Escobar
 */

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler(val priority: EventPriority = EventPriority.NORMAL, val ignoreCancelled: Boolean = false)