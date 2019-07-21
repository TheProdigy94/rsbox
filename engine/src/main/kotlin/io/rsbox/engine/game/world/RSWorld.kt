package io.rsbox.engine.game.world

import io.rsbox.api.GameContext
import io.rsbox.api.Server
import io.rsbox.api.Service
import io.rsbox.api.World
import io.rsbox.engine.event.RSEventFactory
import io.rsbox.util.ServerProperties
import io.rsbox.util.codec.HuffmanCodec
import mu.KLogging
import net.runelite.cache.IndexType
import net.runelite.cache.fs.Store

/**
 * @author Kyle Escobar
 */

class RSWorld(private val server: Server, private val gameContext: GameContext) : World {

    override fun getServer(): Server {
        return this.server
    }

    /**
     * This is the storage for our cache.
     */
    internal lateinit var cacheStore: Store

    // TODO cache definitions and a shit ton after that... O.O

    /**
     * The [HuffmanCodec] used to compress / decompress public chat.
     */
    internal val huffman by lazy {
        val binary = cacheStore.getIndex(IndexType.BINARY)
        val archive = binary.findArchiveByName("huffman")!!
        val file = archive.getFiles(cacheStore.storage.loadArchive(archive)!!).files[0]
            HuffmanCodec(file.contents)
    }

    /**
     * Current Cycle count
     */
    internal var currentCycle = 0

    /**
     * Reboot timer
     */
    internal var rebootTimer = -1

    /**
     * A collection of [Service]s that need to be loaded. These are loaded from services.yml
     */
    internal val services = mutableListOf<Service>()

    override fun init() {

    }

    override fun preLoad() {
        RSEventFactory.callWorldPreloadEvent(this)
    }

    override fun postLoad() {

    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Service> getService(type: Class<out T>, searchSubClasses: Boolean): T? {
        if(searchSubClasses) {
            return services.firstOrNull { type.isAssignableFrom(it::class.java) } as T?
        }
        return services.firstOrNull { it::class.java == type } as T?
    }

    override fun loadServices(server: Server, serviceProperties: ServerProperties) {
        val foundServices = serviceProperties.get<ArrayList<Any>>("services")!!
        foundServices.forEach { service ->
            val values = service as LinkedHashMap<*, *>
            val className = values["class"] as String
            val clazz = Class.forName(className).asSubclass(Service::class.java)!!
            @Suppress("NAME_SHADOWING") val service = clazz.newInstance()

            val properties = hashMapOf<String, Any>()

            values.filterKeys { it != "class" }.forEach { key, value ->
                properties[key as String] = value
            }

            service.init(server, this, ServerProperties().loadMap(properties))
            services.add(service)
            logger.info("Initialized service {}.", service.javaClass.simpleName)
        }

        services.forEach { service -> service.postLoad(server, this) }
        logger.info("Loaded {} engine services.", services.size)
    }

    companion object : KLogging()
}