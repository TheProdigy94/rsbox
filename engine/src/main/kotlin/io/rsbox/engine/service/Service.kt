package io.rsbox.engine.service

import io.rsbox.engine.Server
import io.rsbox.engine.model.World
import io.rsbox.util.ServerProperties

/**
 * Any service that should be initialized when our server is starting up.
 *
 * @author Tom <rspsmods@gmail.com>
 */
interface Service {

    /**
     * Called when the server is starting up.
     */
    fun init(server: Server, world: World, serviceProperties: ServerProperties)

    /**
     * Called after the server has finished started up.
     */
    fun postLoad(server: Server, world: World)

    /**
     * Called after the server sets its bootstrap's children and parameters.
     */
    fun bindNet(server: Server, world: World)

    /**
     * Called when the server is shutting off.
     */
    fun terminate(server: Server, world: World)
}