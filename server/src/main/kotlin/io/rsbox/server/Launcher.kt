package io.rsbox.server

import io.rsbox.api.RSBox
import io.rsbox.api.Server
import io.rsbox.engine.RSServer

/**
 * @author Kyle Escobar
 */

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val server = RSServer() as Server
        server.initServer()

        RSServer.logger.info { "Hooking into RSBox API..." }
        RSBox.setServer(server)
    }
}