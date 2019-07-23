package io.rsbox.api.plugin

import io.rsbox.api.Server
import io.rsbox.api.event.EventException
import io.rsbox.api.event.EventListener
import io.rsbox.api.event.RegisteredListener
import mu.KLogger
import java.io.File

/**
 * @author Kyle Escobar
 */

abstract class RSBoxPlugin : PluginBase() {
    private var _isEnabled = false
    private var _loader: PluginLoader? = null
    private var _server: Server? = null
    private var _file: File? = null
    private var _pluginProperties: PluginPropertiesFile? = null
    private var _dataFolder: File? = null
    private var _classLoader: PluginClassLoader? = null
    private var _logger: KLogger? = null

    private val _listeners = arrayListOf<RegisteredListener>()

    override fun getServer(): Server {
        return this._server!!
    }

    override fun getDataFolder(): File {
        return this._dataFolder!!
    }

    override fun getProperties(): PluginPropertiesFile {
        return this._pluginProperties!!
    }

    override fun getPluginLoader(): PluginLoader {
        return this._loader!!
    }

    override fun isEnabled(): Boolean {
        return this._isEnabled
    }

    fun getClassLoader(): PluginClassLoader {
        return this._classLoader!!
    }

    fun init(loader: PluginLoader, server: Server, pluginProperties: PluginPropertiesFile, dataFolder: File, file: File, classLoader: PluginClassLoader) {
        this._loader = loader
        this._server = server
        this._file = file
        this._pluginProperties = pluginProperties
        this._dataFolder = dataFolder
        this._classLoader = classLoader
        this._logger = this._server!!.getLogger()

        this._loader!!.enablePlugin(this)
    }

    fun setEnabled(enabled: Boolean) {
        if(this._isEnabled != enabled) {
            this._isEnabled = enabled

            if(this._isEnabled) {
                onEnable()
                PluginManager.plugins.add(this)
            } else {
                onDisable()
                PluginManager.plugins.remove(this)
            }
        }
    }

    private fun getRegisteredListener(listener: EventListener): EventListener? {
        this._listeners.forEach { l ->
            if(l.listener.javaClass.simpleName == listener.javaClass.simpleName) {
                return l.listener
            }
        }
        return null
    }

    fun registerListener(listener: EventListener) {
        if(this.getRegisteredListener(listener) != null) {
            throw EventException("Listener ${listener.javaClass.simpleName} has already been registered.")
        }

        this._listeners.add(RegisteredListener(listener, this))
    }

    fun getEventListeners(): ArrayList<RegisteredListener> {
        return this._listeners
    }
}