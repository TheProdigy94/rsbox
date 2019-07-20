package io.rsbox.api

import com.google.common.base.Stopwatch
import io.rsbox.util.ServerProperties
import java.nio.file.Path

/**
 * Interface for the engine to hook into.
 * @author Kyle Escobar
 */
interface Server {
    /**
     * Called to initialize the server related things such as directories and default config files.
     * This method will create the default configs and directories if they do not exist.
     * @return [Boolean] Returns true if everything is ready to start the server. If false, it will print what steps need to be done in the console. The process will not continue.
     */
    fun initServer(): Boolean

    /**
     * Sets the stopwatch for tracking time.
     * @param stopwatch Stopwatch object to overwrite
     */
    fun setStopwatch(stopwatch: Stopwatch)

    /**
     * Gets the [Stopwatch] instance from the engine
     * @return [Stopwatch]
     */
    fun getStopwatch(): Stopwatch

    /**
     * Starts the server networking
     * @param cache File path where the cache is stored.
     * @param serverProperties File path where server.properties.yml is stored.
     * @param packets File path where packets.yml are stored.
     * @param blocks File path where blocks.yml are stored.
     * @param args Launcher arguments
     */
    fun startServer(cache: Path, serverProperties: Path, packets: Path, blocks: Path, args: Array<String>)

    /**
     * Gets and returns the parsed key -> values defined in server.properties.yml
     * @return [ServerProperties]
     */
    fun getServerProperties(): ServerProperties

    /**
     * Gets and return [GameContext] instance from the engine.
     * @return [GameContext]
     */
    fun getGameContext(): GameContext

    /**
     * Gets and returns the [World] instance from the engine.
     * @return [World]
     */
    fun getWorld(): World
}