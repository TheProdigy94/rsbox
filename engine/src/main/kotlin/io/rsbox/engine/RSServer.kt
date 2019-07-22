package io.rsbox.engine

import com.google.common.base.Stopwatch
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.rsbox.api.GameContext
import io.rsbox.api.Server
import io.rsbox.api.World
import io.rsbox.engine.game.world.RSWorld
import io.rsbox.engine.plugin.RSPluginLoader
import io.rsbox.engine.server.ClientChannelInitializer
import io.rsbox.engine.service.rsa.RsaService
import io.rsbox.util.ServerProperties
import mu.KLogging
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ClassPathResource
import org.springframework.util.ResourceUtils
import java.io.File
import java.lang.NullPointerException
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/**
 * @author Kyle Escobar
 */

/**
 * The [RSServer] is the object which makes all the needed calls to initialize the RSBox server
 */
class RSServer : Server {
    private val acceptGroup = NioEventLoopGroup(2)

    private val ioGroup = NioEventLoopGroup(1)

    internal val bootstrap = ServerBootstrap()

    private lateinit var stopwatch: Stopwatch

    private var serverProperties = ServerProperties()
    private var serviceProperties = ServerProperties()

    private lateinit var gameContext: GameContext

    internal lateinit var world: World

    private val pluginLoader = RSPluginLoader(this)

    /**
     * RSBox directories to create
     */
    private val dirs = arrayOf(
        "./rsbox/",
        "./rsbox/config/",
        "./rsbox/data",
        "./rsbox/data/cache",
        "./rsbox/data/xteas",
        "./rsbox/data/rsa",
        "./rsbox/data/saves",
        "./rsbox/data/defs",
        "./rsbox/plugins"
    )

    private val resources = hashMapOf(
        Pair("rsbox/config/server.properties.yml", ClassPathResource("config/server.properties.yml").inputStream),
        Pair("rsbox/config/services.yml", ClassPathResource("config/services.yml").inputStream),
        Pair("rsbox/data/blocks.yml", ClassPathResource("data/blocks.yml").inputStream),
        Pair("rsbox/data/packets.yml", ClassPathResource("data/packets.yml").inputStream),
        Pair("rsbox/data/defs/items.yml", ClassPathResource("data/items.yml").inputStream),
        Pair("rsbox/data/defs/npcs.yml", ClassPathResource("data/npcs.yml").inputStream),
        Pair("rsbox/data/defs/objs.yml", ClassPathResource("data/objs.yml").inputStream)
    )

    /**
     * Initializes anything needed to start the server
     */
    override fun initServer(): Boolean {
        Thread.setDefaultUncaughtExceptionHandler { t, e -> logger.error("Uncaught server exception in thread $t!", e)}
        val stopwatch = Stopwatch.createStarted()

        logger.info { "Starting engine initialization..." }

        /**
         * Check if RSbox directory and sub directories exists.
         * If not, create them.
         */
        dirs.forEach { dir ->
            val file = File(dir)
            if(!file.exists()) {
                stopwatch.reset().start()
                logger.info { "Required directory '${file.path}' does not exist. Creating directory."}
                file.mkdir()
                logger.info("Created directory {} in {}ms.", file.path, stopwatch.elapsed(TimeUnit.MILLISECONDS))
            }
        }

        logger.info { "Scanning folders for required files." }

        resources.forEach { path, resource ->
            val file = File(path)
            if(!file.exists()) {
                FileUtils.copyInputStreamToFile(resource, file)
                IOUtils.closeQuietly(resource)
            }
        }

        logger.info { "RSServer engine initialization completed." }

        return true
    }

    override fun startServer(cache: Path, serverProperties: Path, serviceProperties: Path, packets: Path, blocks: Path, args: Array<String>) {
        this.setStopwatch(Stopwatch.createStarted())

        this.setServerProperties(this.serverProperties.loadYaml(serverProperties.toFile()))
        this.serviceProperties = this.serviceProperties.loadYaml(serviceProperties.toFile())

        logger.info("Loaded server properties for ${this.getServerProperties().get<String>("name")!!} in {}ms.", this.getStopwatch().elapsed(TimeUnit.MILLISECONDS))

        this.getStopwatch().reset().start()
        this.gameContext = RSGameContext(
            name = this.getServerProperties().get<String>("name")!!,
            revision = this.getServerProperties().get<Int>("revision")!!,
            tickSpeed = this.getServerProperties().getOrDefault("tickSpeed", 600),
            maxPlayers = this.getServerProperties().getOrDefault("maxPlayers", 2000)
        )

        logger.info("Loaded game context in {}ms", this.getStopwatch().elapsed(TimeUnit.MILLISECONDS))

        this.getStopwatch().reset().start()
        logger.info("Loading world...")

        world = RSWorld(this, this.getGameContext())
        logger.info("Loaded world in {}ms.", this.getStopwatch().elapsed(TimeUnit.MILLISECONDS))

        logger.info("Preparing to load engine services...")
        world.loadServices(this, serviceProperties = this.serviceProperties)

        logger.info("Unpacking cache...")
        world.setCacheStore(cache.toFile())
        world.getCacheStore().load()
        logger.info("Loaded cache store.")

        logger.info("Loading definitions from cache...")
        world.getDefinitions().loadAll(world.getCacheStore())


        /**
         * Start the server process.
         */
        this.getStopwatch().reset().start()
        logger.info { "Starting server process..." }
        val rsaService = world.getService(RsaService::class.java)
        val clientChannelInitializer = ClientChannelInitializer(
            revision = gameContext.getRevision(),
            rsaExponent = rsaService?.getExponent(),
            rsaModulus = rsaService?.getModulus(),
            cacheStore = world.getCacheStore(),
            world = world as RSWorld
        )
        bootstrap.group(acceptGroup, ioGroup)
        bootstrap.channel(NioServerSocketChannel::class.java)
        bootstrap.childHandler(clientChannelInitializer)
        bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true)

        val port = getServerProperties().getOrDefault("server-port", 43594)
        bootstrap.bind(InetSocketAddress(port)).sync().awaitUninterruptibly()
        logger.info("Server start. Listening for incoming connections on port $port.")

        logger.info("Loading plugins...")
        File("./rsbox/plugins").walk().forEach { file ->
            if(file.extension == "jar") {
                pluginLoader.loadPlugin(file)
            }
        }

        System.gc()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun setStopwatch(stopwatch: Stopwatch) {
        this.stopwatch = stopwatch
    }

    override fun getStopwatch(): Stopwatch {
        return this.stopwatch ?: throw NullPointerException("Stopwatch was called but never set.")
    }

    internal fun setServerProperties(serverProperties: ServerProperties) {
        this.serverProperties = serverProperties
    }

    override fun getServerProperties(): ServerProperties {
        return this.serverProperties
    }

    override fun getGameContext(): GameContext {
        return this.gameContext
    }

    override fun getWorld(): World {
        return this.world
    }

    companion object: KLogging()
}