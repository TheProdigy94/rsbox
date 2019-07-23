package io.rsbox.engine.service.game

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.rsbox.engine.RSServer
import io.rsbox.engine.fs.DefinitionSet
import io.rsbox.engine.fs.def.ObjectDef
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.service.Service
import io.rsbox.util.ServerProperties
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ObjectMetadataService : Service {

    private lateinit var path: Path

    override fun init(server: RSServer, world: RSWorld, serviceProperties: ServerProperties) {
        path = Paths.get(serviceProperties.getOrDefault("path", "./rsbox/data/defs/objs.yml"))
        if (!Files.exists(path)) {
            throw FileNotFoundException("Path does not exist. $path")
        }

        Files.newBufferedReader(path).use { reader ->
            load(world.definitions, reader)
        }
    }

    override fun postLoad(server: RSServer, world: RSWorld) {
    }

    override fun bindNet(server: RSServer, world: RSWorld) {
    }

    override fun terminate(server: RSServer, world: RSWorld) {
    }

    private fun load(definitions: DefinitionSet, reader: BufferedReader) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.propertyNamingStrategy = PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        val reference = object : TypeReference<List<Metadata>>() {}
        mapper.readValue<List<Metadata>>(reader, reference)?.let { metadataSet ->
            metadataSet.forEach { metadata ->
                val def = definitions.getNullable(ObjectDef::class.java, metadata.id) ?: return@forEach
                def.examine = metadata.examine
            }
        }
    }

    private data class Metadata(val id: Int = -1, val examine: String? = null)
}