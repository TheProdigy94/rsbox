package io.rsbox.api.plugin

import io.rsbox.api.Server
import mu.KLogging
import org.yaml.snakeyaml.error.YAMLException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Pattern

/**
 * @author Kyle Escobar
 */

class  PluginLoader(val server: Server) {
    private val fileFilters = arrayOf<Pattern>(
        Pattern.compile("\\.jar$")
    )

    private val classes: Map<String, Class<*>> = ConcurrentHashMap()
    private val loaders = CopyOnWriteArrayList<PluginClassLoader>()

    fun loadPlugin(file: File) {
        if(!file.exists()) {
            throw FileNotFoundException("${file.path} does not exist.")
        }

        val config = loadPluginConfigFile(file)

        val dataFolder = File(file.parentFile, config.get<String>("name"))

        if(!dataFolder.exists()) {
            dataFolder.mkdirs()
            KLogging().logger.info { "${config.get<String>("name")} plugin folder did not exist. Creating one." }
        }

        val loader: PluginClassLoader
                try {
                    loader = PluginClassLoader(this, this.javaClass.classLoader, config, dataFolder, file)
                } catch(e : InvalidPluginException) {
                    throw e
                } catch( e : Throwable) {
                    throw InvalidPluginException("", e)
                }

        loaders.add(loader)
    }

    fun loadPluginConfigFile(file: File): PluginPropertiesFile {
        var jar: JarFile? = null
        var entry: JarEntry? = null
        var stream: InputStream? = null
        try {
            jar = JarFile(file)
            entry = jar.getJarEntry("plugin.yml")

            if(entry == null) {
                throw FileNotFoundException("Jar does not contain plugin.yml")
            }

            stream = jar.getInputStream(entry)

            return PluginPropertiesFile(stream).loadYaml()
        } catch (e: IOException) {
            throw IOException(e)
        } catch(e: YAMLException) {
            throw YAMLException(e)
        } finally {
            if(jar != null) {
                try {
                    jar.close()
                } catch(e: IOException) {}
            }
            if(stream != null) {
                try {
                    stream.close()
                } catch(e: IOException) {}
            }
        }
    }
}