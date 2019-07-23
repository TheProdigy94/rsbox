package io.rsbox.api.plugin

import io.rsbox.api.Server
import mu.KLogger
import java.io.File
import java.io.InputStream

/**
 * @author Kyle Escobar
 */

interface Plugin {
    fun getDataFolder(): File

    fun getProperties(): PluginPropertiesFile

    fun isEnabled(): Boolean

    fun getPluginLoader(): PluginLoader

    fun getServer(): Server

    fun onEnable()

    fun onDisable()

    fun getName(): String
}