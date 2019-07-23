package io.rsbox.api.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.InputStream

/**
 * @author Kyle Escobar
 */

class PluginPropertiesFile(private val inputStream: InputStream) {
    private val properties = hashMapOf<String, Any?>()

    @Suppress("UNCHECKED_CAST")
    fun <T> getOrDefault(key: String, default: T): T = properties[key] as? T ?: default

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? = properties[key] as? T

    fun loadYaml(): PluginPropertiesFile {
        check(properties.isEmpty())

        val content = inputStream.bufferedReader().use { it.readText() }

        val mapper = ObjectMapper(YAMLFactory())
        val values = mapper.readValue(content, HashMap<String, Any>().javaClass)

        values.forEach { key, value ->
            if(value is String && value.isEmpty()) {
                properties[key] = null
            } else {
                properties[key] = value
            }
        }

        return this
    }
}