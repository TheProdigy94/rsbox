package io.rsbox.engine

import com.google.common.base.Stopwatch
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import mu.KLogging
import org.springframework.util.ResourceUtils
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * @author Kyle Escobar
 */

/**
 * The [Server] is the object which makes all the needed calls to initialize the RSBox server
 */
class Server {
    private val acceptGroup = NioEventLoopGroup(2)

    private val ioGroup = NioEventLoopGroup(1)

    val bootstrap = ServerBootstrap()

    /**
     * RSBox directories to create
     */
    private val dirs = arrayOf(
        "./rsbox/",
        "./rsbox/configs/"
    )

    /**
     * Initializes anything needed to start the server
     */
    fun initServer() {
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
        val serverPropFile = File("./rsbox/configs/server.properties.yml")
        val defaultPropFile = ResourceUtils.getFile("classpath:configs/server.properties.yml")

        println(defaultPropFile)

        if(!serverPropFile.exists()) {
            stopwatch.reset().start()
            logger.info { "Server properties file 'server.properties.yml' not found. Creating default." }
            defaultPropFile.copyTo(serverPropFile)
            logger.info("Created default '{}' file in {}ms.", serverPropFile.name, stopwatch.elapsed(TimeUnit.MILLISECONDS))
        }

        logger.info { "Server engine initialization completed." }
    }

    companion object: KLogging()
}