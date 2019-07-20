package io.rsbox.engine

import com.google.common.base.Stopwatch
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.rsbox.api.GameContext
import io.rsbox.api.Server
import io.rsbox.api.World
import io.rsbox.engine.game.world.RSWorld
import io.rsbox.util.ServerProperties
import mu.KLogging
import org.springframework.util.ResourceUtils
import java.io.File
import java.lang.NullPointerException
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

    private lateinit var gameContext: GameContext

    internal lateinit var world: World

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
        ".rsbox/data/saves",
        ".rsbox/data/defs"
    )

    private val resources = hashMapOf(
        Pair("./rsbox/data/blocks.yml", ResourceUtils.getFile("classpath:data/blocks.yml")),
        Pair("./rsbox/data/packets.yml", ResourceUtils.getFile("classpath:data/packets.yml")),
        Pair("./rsbox/data/defs/items.yml", ResourceUtils.getFile("classpath:data/items.yml")),
        Pair("./rsbox/data/defs/npcs.yml", ResourceUtils.getFile("classpath:data/npcs.yml")),
        Pair("./rsbox/data/defs/objs.yml", ResourceUtils.getFile("classpath:data/objs.yml"))
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

        /**
         * Check if server.properties.yml exists
         * If not, clone from sources. [projectRoot]/server.properties.yml
         */
        val serverPropFile = File("./rsbox/config/server.properties.yml")
        val defaultPropFile = ResourceUtils.getFile("classpath:config/server.properties.yml")

        println(defaultPropFile)

        if(!serverPropFile.exists()) {
            stopwatch.reset().start()
            logger.info { "RSServer properties file 'server.properties.yml' not found. Creating default." }
            defaultPropFile.copyTo(serverPropFile)
            logger.info("Created default '{}' file in {}ms.", serverPropFile.name, stopwatch.elapsed(TimeUnit.MILLISECONDS))
        }

        resources.forEach { path, resource ->
            val file = File(path)
            if(!file.exists()) {
                resource.copyTo(file)
            }
        }

        logger.info { "RSServer engine initialization completed." }

        return true
    }

    override fun startServer(cache: Path, serverProperties: Path, packets: Path, blocks: Path, args: Array<String>) {
        this.setStopwatch(Stopwatch.createStarted())

        this.setServerProperties(this.serverProperties.loadYaml(serverProperties.toFile()))

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

        world = RSWorld(this.getGameContext())
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