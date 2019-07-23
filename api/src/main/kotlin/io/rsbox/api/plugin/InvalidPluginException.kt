package io.rsbox.api.plugin

/**
 * @author Kyle Escobar
 */

class InvalidPluginException(override val message: String, override val cause: Throwable) : Exception(message, cause)