package io.rsbox.engine.service

import io.rsbox.engine.RSServer
import io.rsbox.engine.model.RSWorld
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
    fun init(server: RSServer, world: RSWorld, serviceProperties: ServerProperties)

    /**
     * Called after the server has finished started up.
     */
    fun postLoad(server: RSServer, world: RSWorld)

    /**
     * Called after the server sets its bootstrap's children and parameters.
     */
    fun bindNet(server: RSServer, world: RSWorld)

    /**
     * Called when the server is shutting off.
     */
    fun terminate(server: RSServer, world: RSWorld)
}