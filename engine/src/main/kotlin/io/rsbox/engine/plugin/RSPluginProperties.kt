package io.rsbox.engine.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import jdk.internal.util.xml.impl.Input
import java.io.InputStream

/**
 * @author Kyle Escobar
 */

@Suppress("UNCHECKED_CAST")
class RSPluginProperties(val inputStream: InputStream) {
    private val properties = hashMapOf<String, Any?>()

    fun <T> getOrDefault(key: String, default: T): T = properties[key] as? T ?: default

    fun <T> get(key: String): T? = properties[key] as? T

    fun has(key: String): Boolean = properties.containsKey(key)

    fun loadYaml(): RSPluginProperties {
        check(properties.isEmpty())

        val content = inputStream.bufferedReader().use { it.readText() }

        val mapper = ObjectMapper(YAMLFactory())
        val values = mapper.readValue(content, HashMap<String, Any>().javaClass)

        values.forEach { key,value ->
            if(value is String && value.isEmpty()) {
                properties[key] = null
            } else {
                properties[key] = value
            }
        }
        return this
    }
}