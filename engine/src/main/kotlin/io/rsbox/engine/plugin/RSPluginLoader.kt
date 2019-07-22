package io.rsbox.engine.plugin

import io.rsbox.api.Server
import mu.KLogging
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.jar.JarFile
import java.util.regex.Pattern

/**
 * @author Kyle Escobar
 */

class RSPluginLoader(val server: Server) {
    private val fileFilters = arrayOf<Pattern>(
        Pattern.compile("\\.jar$")
    )

    private val classes: Map<String, Class<*>> = ConcurrentHashMap()
    private val loaders: List<RSPluginClassLoader> = CopyOnWriteArrayList()

    fun loadPlugin(file: File) {
        if(!file.exists()) {
            throw FileNotFoundException("${file.path} does not exist.")
        }

        val config = loadPluginConfigFile(file)

        val dataFolder = File(file.parentFile, config.getName())

        if(!dataFolder.exists()) {
            dataFolder.mkdirs()
            KLogging().logger.info { "${config.getName()} plugin folder did not exist. Creating one." }
        }

        // TODO Load classes and create / store plugin main class instance.

        KLogging().logger.info { "Loaded plugin ${config.getName()}." }

    }

    fun loadPluginConfigFile(file: File): RSPluginProperties {
        val jar = JarFile(file)
        val entry = jar.getJarEntry("plugin.yml")

        if(entry == null) {
            throw FileNotFoundException("Jar does not contain plugin.yml")
        }

        val stream = jar.getInputStream(entry)

        jar.close()
        stream.close()

        return RSPluginProperties(stream)
    }
}