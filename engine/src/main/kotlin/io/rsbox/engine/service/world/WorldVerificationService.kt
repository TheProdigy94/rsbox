package io.rsbox.engine.service.world

import io.rsbox.engine.RSServer
import io.rsbox.engine.model.PlayerUID
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.service.Service
import io.rsbox.net.codec.login.LoginResultType
import io.rsbox.util.ServerProperties

/**
 * @author Tom <rspsmods@gmail.com>
 */
interface WorldVerificationService : Service {

    override fun init(server: RSServer, world: RSWorld, serviceProperties: ServerProperties) {
    }

    override fun postLoad(server: RSServer, world: RSWorld) {
    }

    override fun bindNet(server: RSServer, world: RSWorld) {
    }

    override fun terminate(server: RSServer, world: RSWorld) {
    }

    /**
     * Intercept the login result on a player log-in.
     *
     * @return null if the player can log in successfully without
     */
    fun interceptLoginResult(world: RSWorld, uid: PlayerUID, displayName: String, loginName: String): LoginResultType?
}