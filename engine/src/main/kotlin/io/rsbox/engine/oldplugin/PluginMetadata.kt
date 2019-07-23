package io.rsbox.engine.oldplugin

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Contains data relevant to a [Plugin].
 *
 * @param propertyFileName the file name that will store the metadata. This should
 * be the <strong>file-name</strong>, not the full path and not including the
 * extension. This name should be unique enough so that no two plugins would
 * ever share the same name, otherwise there may be conflicting issues.
 *
 * @param name the name of our oldplugin. There are no restrictions to what you can
 * name a oldplugin.
 *
 * @param description a short description of the oldplugin.
 *
 * @param authors a collection of the oldplugin authors.
 *
 * @param properties a map of properties that can be accessed by the oldplugin.
 *
 * @author Tom <rspsmods@gmail.com>
 */
data class PluginMetadata(
        @Transient val propertyFileName: String?,
        @JsonProperty("name") val name: String?,
        @JsonProperty("description") val description: String?,
        @JsonProperty("authors") val authors: Set<String>,
        @JsonProperty("properties") val properties: Map<String, Any>)