package io.rsbox.api.event

import java.lang.Exception

/**
 * @author Kyle Escobar
 */

class EventException(override val message: String) : Exception(message) {
}