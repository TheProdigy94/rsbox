package io.rsbox.engine

import io.rsbox.api.RSBox
import io.rsbox.api.Server
import java.nio.file.Paths

/**
 * @author Kyle Escobar
 */

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val server = RSServer() as Server
        RSServer.logger.info { "Hooking into RSBox API..." }
        RSBox.setServer(server)

        server.initServer()

        server.startServer(
            cache = Paths.get("./rsbox/data/cache"),
            serverProperties = Paths.get("./rsbox/config/server.properties.yml"),
            serviceProperties = Paths.get("./rsbox/config/services.yml"),
            packets = Paths.get("./rsbox/data/packets.yml"),
            blocks = Paths.get("./rsbox/data/blocks.yml"),
            args = args
        )
    }
}