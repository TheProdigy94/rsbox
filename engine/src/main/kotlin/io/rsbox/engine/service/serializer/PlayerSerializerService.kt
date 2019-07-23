package io.rsbox.engine.service.serializer

import io.rsbox.engine.Server
import io.rsbox.engine.model.Tile
import io.rsbox.engine.model.World
import io.rsbox.engine.model.attr.NEW_ACCOUNT_ATTR
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

    private lateinit var startTile: Tile

    final override fun init(server: Server, world: World, serviceProperties: ServerProperties) {
        startTile = Tile(world.gameContext.home)
        initSerializer(server, world, serviceProperties)
    }

    override fun postLoad(server: Server, world: World) {
    }

    override fun bindNet(server: Server, world: World) {
    }

    override fun terminate(server: Server, world: World) {
    }

    fun configureNewPlayer(client: Client, request: LoginRequest) {
        client.attr.put(NEW_ACCOUNT_ATTR, true)

        client.passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt(16))
        client.tile = startTile
    }

    abstract fun initSerializer(server: Server, world: World, serviceProperties: ServerProperties)

    abstract fun loadClientData(client: Client, request: LoginRequest): PlayerLoadResult

    abstract fun saveClientData(client: Client): Boolean
}