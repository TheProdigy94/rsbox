package io.rsbox.api

/**
 * Interface for the engine to hook into.
 * @author Kyle Escobar
 */
interface Server {
    /**
     * Called to initialize the server related things such as directories and default config files.
     * This method will create the default configs and directories if they do not exist.
     */
    fun initServer()
}