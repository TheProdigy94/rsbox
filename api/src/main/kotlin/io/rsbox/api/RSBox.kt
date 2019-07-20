package io.rsbox.api

/**
 * [RSBox] is the api which holds the instance of the server where all the other API branches from.
 *
 * @author Kyle Escobar
 */
object RSBox {
    private var server: Server? = null

    /**
     * Sets the singleton instance of [Server] from the engine module.
     * @param serverInstance Instance of [Server] from the engine module.
     * @throws UnsupportedOperationException If the server singleton instance has already been defined.
     */
    fun setServer(serverInstance: Server) {
        if(server != null) { throw UnsupportedOperationException("Cannot redefine singleton Server") }
        this.server = serverInstance
    }

    /**
     * Gets the singleton instance of [Server] from memory and return it.
     * @return [Server] Returns [Server] instance from engine module.
     * @throws UnsupportedOperationException If the server singleton instance has not yet been defined.
     */
    fun getServer(): Server {
        return this.server ?: throw UnsupportedOperationException("Server singleton has not been defined. Something has gone wrong.")
    }
}