package io.rsbox.engine

import java.nio.file.Paths

object Launcher {

    @JvmStatic
    fun main(args: Array<String>) {
        val server = RSServer()
        server.startServer()
        server.startGame(
                filestore = Paths.get("./rsbox/data", "cache"),
                gameProps = Paths.get("./rsbox/config/server.properties.yml"),
                packets = Paths.get("./rsbox/data/packets.yml"),
                blocks = Paths.get("./rsbox/data/blocks.yml"),
                devProps = Paths.get("./rsbox/config/dev-settings.yml"),
                args = args)
    }
}