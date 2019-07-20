package io.rsbox.api.event

/**
 * The exception throws if an event has a problem.
 * @exception EventException
 *
 * @author Kyle Escobar
 */
class EventException(override val message: String, override var cause: Throwable? = null) : Exception()