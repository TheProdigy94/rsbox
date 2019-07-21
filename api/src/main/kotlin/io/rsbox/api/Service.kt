package io.rsbox.api

import io.rsbox.util.ServerProperties

/**
 * @author Kyle Escobar
 */

interface Service {
    fun init(server: Server, world: World, serverProperties: ServerProperties)

    fun postLoad(server: Server, world: World)

    fun bindNet(server: Server, world: World)

    fun terminate(server: Server, world: World)
}