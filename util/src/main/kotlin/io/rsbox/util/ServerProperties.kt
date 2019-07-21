package io.rsbox.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File

/**
 * @author Kyle Escobar
 */

/**
 * [ServerProperties] defines the settings used by the Engine
 */
class ServerProperties {
    private val properties = hashMapOf<String, Any?>()

    /**
     * Gets a property value associated with [key]. Retruns [default] if value is not defined
     * @param key           Key property as string
     * @param default       Default value if [key] is not found
     *
     * @return [T]          Returns generic type
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getOrDefault(key: String, default: T): T = properties[key] as? T ?: default

    /**
     * Gets a property value associated with [key].
     * @param key           Key property as String
     *
     * @return [T]          Returns generic type
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? = properties[key] as? T

    /**
     * Checks to see if a given property [key] exists.
     *
     * @param key           Key property as String
     *
     * @return [Boolean]    Returns boolean
     */
    fun has(key: String): Boolean = properties.containsKey(key)

    /**
     * Loads a [File] and parses it into [ServerProperties] instance
     * @param File          [File] to load. Must be (.yml)
     *
     * @return [ServerProperties]
     */
    fun loadYaml(file: File): ServerProperties {
        check(properties.isEmpty())

        val mapper = ObjectMapper(YAMLFactory())
        val values = mapper.readValue(file, HashMap<String, Any>().javaClass)

        values.forEach { key, value ->
            if(value is String && value.isEmpty()) {
                properties[key] = null
            } else {
                properties[key] = value
            }
        }

        return this
    }

    fun loadMap(data: Map<String, Any>): ServerProperties {
        check(properties.isEmpty())

        data.forEach { key, value ->
            if(value is String && value.isEmpty()) {
                properties[key] = null
            } else {
                properties[key] = value
            }
        }

        return this
    }
}