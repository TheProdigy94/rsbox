package io.rsbox.engine

/**
 * @author Kyle Escobar
 */

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val server = Server()
        server.initServer()
    }
}