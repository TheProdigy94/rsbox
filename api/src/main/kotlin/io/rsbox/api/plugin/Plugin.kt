package io.rsbox.api.plugin

import io.rsbox.api.Server
import mu.KLogging
import java.io.File
import java.io.InputStream

/**
 * @author Kyle Escobar
 */

interface Plugin {
    fun getDataFolder(): File

    fun getProperties(): PluginPropertiesFile

    fun getResource(filename: String): InputStream

    fun saveConfig()

    fun saveResource()

    fun getPluginLoader(): PluginLoader

    fun getServer(): Server

    fun isEnabled(): Boolean

    fun onLoad()

    fun onEnable()

    fun onDisable()

    fun getLogger(): KLogging

    fun getName(): String
}