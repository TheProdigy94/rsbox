package io.rsbox.api

import io.rsbox.util.ServerProperties

/**
 * @author Kyle Escobar
 */

interface World {
    fun getServer(): Server

    fun init()

    fun preLoad()

    fun postLoad()

    fun <T : Service> getService(type: Class<out T>, searchSubClasses: Boolean = false): T?

    fun loadServices(server: Server, serviceProperties: ServerProperties)
}