package io.rsbox.engine.system

import io.rsbox.engine.model.World
import io.rsbox.engine.service.login.LoginService
import io.rsbox.net.codec.login.LoginRequest
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext

/**
 * A [ServerSystem] responsible for submitting the [LoginRequest] to be handled.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class LoginSystem(channel: Channel, private val world: World) : ServerSystem(channel) {

    override fun receiveMessage(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is LoginRequest) {
            if (loginService == null) {
                loginService = world.getService(LoginService::class.java)
            }
            loginService!!.addLoginRequest(world, msg)
        }
    }

    override fun terminate() {
    }

    companion object {
        private var loginService: LoginService? = null
    }
}