package io.rsbox.engine.service.world

import io.rsbox.engine.Server
import io.rsbox.engine.model.PlayerUID
import io.rsbox.engine.model.World
import io.rsbox.engine.service.Service
import io.rsbox.net.codec.login.LoginResultType
import io.rsbox.util.ServerProperties

/**
 * @author Tom <rspsmods@gmail.com>
 */
interface WorldVerificationService : Service {

    override fun init(server: Server, world: World, serviceProperties: ServerProperties) {
    }

    override fun postLoad(server: Server, world: World) {
    }

    override fun bindNet(server: Server, world: World) {
    }

    override fun terminate(server: Server, world: World) {
    }

    /**
     * Intercept the login result on a player log-in.
     *
     * @return null if the player can log in successfully without
     */
    fun interceptLoginResult(world: World, uid: PlayerUID, displayName: String, loginName: String): LoginResultType?
}