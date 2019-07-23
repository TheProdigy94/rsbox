package io.rsbox.engine.service.world

import io.rsbox.engine.model.PlayerUID
import io.rsbox.engine.model.RSWorld
import io.rsbox.net.codec.login.LoginResultType

/**
 * @author Tom <rspsmods@gmail.com>
 */
class SimpleWorldVerificationService : WorldVerificationService {

    override fun interceptLoginResult(world: RSWorld, uid: PlayerUID, displayName: String, loginName: String): LoginResultType? = when {
        world.rebootTimer != -1 && world.rebootTimer < RSWorld.REJECT_LOGIN_REBOOT_THRESHOLD -> LoginResultType.SERVER_UPDATE
        world.getPlayerForName(displayName) != null -> LoginResultType.ALREADY_ONLINE
        world.players.count() >= world.players.capacity -> LoginResultType.MAX_PLAYERS
        else -> null
    }
}