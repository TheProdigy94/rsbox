package io.rsbox.api.plugin

import mu.KLogger
import mu.KLogging

/**
 * @author Kyle Escobar
 */

abstract class RSBoxPlugin {

    private val logger = KLogging().logger("PluginName")

    fun getLogger(): KLogger {
        return this.logger
    }

    abstract fun onEnable()

    abstract fun onDisable()
}