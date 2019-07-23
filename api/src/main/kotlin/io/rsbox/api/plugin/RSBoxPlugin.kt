package io.rsbox.api.plugin

import io.rsbox.api.Server
import mu.KLogger
import java.io.File

/**
 * @author Kyle Escobar
 */

abstract class RSBoxPlugin() : PluginBase() {
    private var _isEnabled = false
    private var _loader: PluginLoader? = null
    private var _server: Server? = null
    private var _file: File? = null
    private var _pluginProperties: PluginPropertiesFile? = null
    private var _dataFolder: File? = null
    private var _classLoader: PluginClassLoader? = null
    private var _logger: KLogger? = null

    init {
        val classLoader = this.javaClass.classLoader
        if(classLoader is PluginClassLoader) {

        }
    }

    constructor(loader: PluginLoader, pluginProperties: PluginPropertiesFile, dataFolder: File, file: File) : this() {

    }

    fun init(loader: PluginLoader, server: Server, pluginProperties: PluginPropertiesFile, dataFolder: File, file: File, classLoader: PluginClassLoader) {
        this._loader = loader
        this._server = server
        this._file = file
        this._pluginProperties = pluginProperties
        this._dataFolder = dataFolder
        this._classLoader = classLoader
    }
}