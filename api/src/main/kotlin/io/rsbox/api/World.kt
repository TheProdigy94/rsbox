package io.rsbox.api

import io.rsbox.util.ServerProperties
import net.runelite.cache.fs.Store
import java.io.File

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

    fun getCacheStore(): Store

    fun setCacheStore(file: File)

    fun setXteaKeyService(service: XteaKeyService)

    fun getXteaKeyService(): XteaKeyService

    fun getDefinitions(): DefinitionSet

    fun setDefinitions(defs: DefinitionSet)
}