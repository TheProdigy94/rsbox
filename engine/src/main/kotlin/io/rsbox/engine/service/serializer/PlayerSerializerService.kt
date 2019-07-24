package io.rsbox.engine.service.serializer

import io.rsbox.engine.RSServer
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.NEW_ACCOUNT_ATTR
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.service.Service
import io.rsbox.net.codec.login.LoginRequest
import io.rsbox.util.ServerProperties
import org.mindrot.jbcrypt.BCrypt

/**
 * A [Service] that is responsible for encoding and decoding player data.
 *
 * @author Tom <rspsmods@gmail.com>
 */
abstract class PlayerSerializerService : Service {

    private lateinit var startTile: RSTile

    final override fun init(server: RSServer, world: RSWorld, serviceProperties: ServerProperties) {
        startTile = RSTile(world.gameContext.home)
        initSerializer(server, world, serviceProperties)
    }

    override fun postLoad(server: RSServer, world: RSWorld) {
    }

    override fun bindNet(server: RSServer, world: RSWorld) {
    }

    override fun terminate(server: RSServer, world: RSWorld) {
    }

    fun configureNewPlayer(client: Client, request: LoginRequest) {
        client.attr.put(NEW_ACCOUNT_ATTR, true)

        client.passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt(16))
        client.tile = startTile
    }

    abstract fun initSerializer(server: RSServer, world: RSWorld, serviceProperties: ServerProperties)

    abstract fun loadClientData(client: Client, request: LoginRequest): PlayerLoadResult

    abstract fun saveClientData(client: Client): Boolean
}