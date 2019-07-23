package io.rsbox.engine

import com.google.common.base.Stopwatch
import io.rsbox.engine.model.Tile
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.GroundItem
import io.rsbox.engine.model.entity.Npc
import io.rsbox.engine.model.skill.SkillSet
import io.rsbox.engine.protocol.ClientChannelInitializer
import io.rsbox.engine.service.GameService
import io.rsbox.engine.service.rsa.RsaService
import io.rsbox.engine.service.xtea.XteaKeyService
import io.rsbox.util.ServerProperties
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.rsbox.api.Server
import io.rsbox.api.World
import mu.KLogger
import mu.KLogging
import net.runelite.cache.fs.Store
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ClassPathResource
import java.io.File
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 * The [RSServer] is responsible for starting any and all games.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class RSServer : Server {

    private val acceptGroup = NioEventLoopGroup(2)

    private val ioGroup = NioEventLoopGroup(1)

    val bootstrap = ServerBootstrap()

    internal lateinit var world: RSWorld

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
        Pair("rsbox/config/dev-settings.yml", ClassPathResource("config/dev-settings.yml").inputStream),
        Pair("rsbox/data/blocks.yml", ClassPathResource("data/blocks.yml").inputStream),
        Pair("rsbox/data/packets.yml", ClassPathResource("data/packets.yml").inputStream),
        Pair("rsbox/data/defs/items.yml", ClassPathResource("data/items.yml").inputStream),
        Pair("rsbox/data/defs/npcs.yml", ClassPathResource("data/npcs.yml").inputStream),
        Pair("rsbox/data/defs/objs.yml", ClassPathResource("data/objs.yml").inputStream)
    )

    /**
     * Prepares and handles any API related logic that must be handled
     * before the game can be launched properly.
     */
    fun startServer() {
        Thread.setDefaultUncaughtExceptionHandler { t, e -> logger.error("Uncaught server exception in thread $t!", e) }
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
    }

    /**
     * Prepares and handles any game related logic that was specified by the
     * user.
     *
     * Due to being decoupled from the API logic that will always be used, you
     * can start multiple servers with different game property files.
     */
    fun startGame(filestore: Path, gameProps: Path, packets: Path, blocks: Path, devProps: Path?, args: Array<String>): RSWorld {
        val stopwatch = Stopwatch.createStarted()
        val individualStopwatch = Stopwatch.createUnstarted()

        /*
         * Load the game property file.
         */
        val initialLaunch = Files.deleteIfExists(Paths.get("./first-launch"))
        val gameProperties = ServerProperties()
        val devProperties = ServerProperties()
        gameProperties.loadYaml(gameProps.toFile())
        if (devProps != null && Files.exists(devProps)) {
            devProperties.loadYaml(devProps.toFile())
        }
        logger.info("Loaded properties for ${gameProperties.get<String>("name")!!}.")

        /*
         * Create a game context for our configurations and services to run.
         */
        val gameContext = GameContext(initialLaunch = initialLaunch,
                name = gameProperties.get<String>("name")!!,
                revision = gameProperties.get<Int>("revision")!!,
                cycleTime = gameProperties.getOrDefault("cycle-time", 600),
                playerLimit = gameProperties.getOrDefault("max-players", 2048),
                home = Tile(gameProperties.get<Int>("home-x")!!, gameProperties.get<Int>("home-z")!!, gameProperties.getOrDefault("home-height", 0)),
                skillCount = gameProperties.getOrDefault("skill-count", SkillSet.DEFAULT_SKILL_COUNT),
                npcStatCount = gameProperties.getOrDefault("npc-stat-count", Npc.Stats.DEFAULT_NPC_STAT_COUNT),
                runEnergy = gameProperties.getOrDefault("run-energy", true),
                gItemPublicDelay = gameProperties.getOrDefault("gitem-public-spawn-delay", GroundItem.DEFAULT_PUBLIC_SPAWN_CYCLES),
                gItemDespawnDelay = gameProperties.getOrDefault("gitem-despawn-delay", GroundItem.DEFAULT_DESPAWN_CYCLES),
                preloadMaps = gameProperties.getOrDefault("preload-maps", false))

        val devContext = DevContext(
                debugExamines = devProperties.getOrDefault("debug-examines", false),
                debugObjects = devProperties.getOrDefault("debug-objects", false),
                debugButtons = devProperties.getOrDefault("debug-buttons", false),
                debugItemActions = devProperties.getOrDefault("debug-items", false),
                debugMagicSpells = devProperties.getOrDefault("debug-spells", false))

        world = RSWorld(gameContext, devContext)

        /*
         * Load the file store.
         */
        individualStopwatch.reset().start()
        world.filestore = Store(filestore.toFile())
        world.filestore.load()
        logger.info("Loaded filestore from path {} in {}ms.", filestore, individualStopwatch.elapsed(TimeUnit.MILLISECONDS))

        /*
         * Load the definitions.
         */
        world.definitions.loadAll(world.filestore)

        /*
         * Load the services required to run the server.
         */
        world.loadServices(this, gameProperties)
        world.init()

        if (gameContext.preloadMaps) {
            /*
             * Preload region definitions.
             */
            world.getService(XteaKeyService::class.java)?.let { service ->
                world.definitions.loadRegions(world, world.chunks, service.validRegions)
            }
        }

        /*
         * Load the packets for the game.
         */
        world.getService(type = GameService::class.java)?.let { gameService ->
            individualStopwatch.reset().start()
            gameService.messageStructures.load(packets.toFile())
            gameService.messageEncoders.init()
            gameService.messageDecoders.init(gameService.messageStructures)
            logger.info("Loaded message codec and handlers in {}ms.", individualStopwatch.elapsed(TimeUnit.MILLISECONDS))
        }

        /*
         * Load the update blocks for the game.
         */
        individualStopwatch.reset().start()
        world.loadUpdateBlocks(blocks.toFile())
        logger.info("Loaded update blocks in {}ms.", individualStopwatch.elapsed(TimeUnit.MILLISECONDS))

        /*
         * Load the privileges for the game.
         */
        individualStopwatch.reset().start()
        world.privileges.load(gameProperties)
        logger.info("Loaded {} privilege levels in {}ms.", world.privileges.size(), individualStopwatch.elapsed(TimeUnit.MILLISECONDS))

        /*
         * Load the plugins for game content.
         */
        individualStopwatch.reset().start()
        world.plugins.init(
                server = this, world = world,
                jarPluginsDirectory = gameProperties.getOrDefault("oldplugin-packed-path", "./plugins"))
        logger.info("Loaded {} plugins in {}ms.", DecimalFormat().format(world.plugins.getPluginCount()), individualStopwatch.elapsed(TimeUnit.MILLISECONDS))

        /*
         * Post load world.
         */
        world.postLoad()

        /*
         * Inform the time it took to load up all non-network logic.
         */
        logger.info("${gameProperties.get<String>("name")!!} loaded up in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms.")

        /*
         * Set our bootstrap's groups and parameters.
         */
        val rsaService = world.getService(RsaService::class.java)
        val clientChannelInitializer = ClientChannelInitializer(revision = gameContext.revision,
                rsaExponent = rsaService?.getExponent(), rsaModulus = rsaService?.getModulus(),
                filestore = world.filestore, world = world)

        bootstrap.group(acceptGroup, ioGroup)
        bootstrap.channel(NioServerSocketChannel::class.java)
        bootstrap.childHandler(clientChannelInitializer)
        bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true)

        /*
         * Bind all service networks, if applicable.
         */
        world.bindServices(this)

        /*
         * Bind the game port.
         */
        val port = gameProperties.getOrDefault("game-port", 43594)
        bootstrap.bind(InetSocketAddress(port)).sync().awaitUninterruptibly()
        logger.info("Now listening for incoming connections on port $port...")

        System.gc()

        return world
    }

    override fun getWorld(): World {
        return world
    }

    override fun getLogger(): KLogger {
        return logger
    }

    companion object : KLogging()
}
