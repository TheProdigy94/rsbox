package io.rsbox.api

/**
 * @author Kyle Escobar
 */

interface GameContext {
    /**
     * Gets the name of the server
     * @return [String]
     */
    fun getName(): String

    /**
     * Gets the cache revision version
     * @return [Int]
     */
    fun getRevision(): Int

    /**
     * Gets the tick speed in <strong>MILLISECONDS</strong>
     * @return [Int]
     */
    fun getTickSpeed(): Int

    /**
     * Gets the maximum number of players that can connect to this server.
     * @return [Int]
     */
    fun getMaxPlayers(): Int
}