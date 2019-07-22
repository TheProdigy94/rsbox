package io.rsbox.engine.plugin

import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.lang.Exception
import java.util.regex.Pattern

/**
 * @author Kyle Escobar
 */

class RSPluginProperties(val inputStream: InputStream) {

    private val VALID_NAME = Pattern.compile("^[A-Za-z0-9 _.-]+$")
    private val YAML: ThreadLocal<Yaml> = ThreadLocal()

    private lateinit var name: String
    private lateinit var main: String
    private lateinit var description: String
    private lateinit var version: String
    private var revision: Int = 0
    private var authors: ArrayList<String> = arrayListOf()

    init {
        loadMap(asMap(YAML.get().load(inputStream)))
    }

    private fun loadMap(map: Map<*, *>) {
        name = map.get("name").toString()

        if(!VALID_NAME.matcher(name).matches()) {
            throw Exception("name '$name' contains invalid characters.")
        }

        name = name.replace(" ", "_")

        version = map.get("version").toString()

        main = map.get("main").toString()

        description = map.get("description").toString()

        revision = map.get("revision").toString().toInt()

        if(map.get("authors") != null) {
            if(map.get("author") != null) {
                authors.add(map.get("author").toString())
            }

            @Suppress("UNCHECKED_CAST")
            (map.get("authors") as Array<String>).forEach { author ->
                authors.add(author)
            }
        }
    }

    private fun asMap(obj: Any): Map<*, *> {
        if(obj is Map<*, *>) {
            return obj
        }
        throw Exception("$obj is not properly structured.")
    }

    fun getName(): String { return name }
    fun getMainClass(): String { return main }
    fun getVersion(): String { return version }
    fun getDescription(): String { return description }
    fun getRevision(): Int { return revision }
    fun getAuthors(): ArrayList<String> { return authors }
}