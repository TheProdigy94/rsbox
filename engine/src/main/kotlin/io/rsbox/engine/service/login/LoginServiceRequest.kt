package io.rsbox.engine.service.login

import io.rsbox.engine.model.RSWorld
import io.rsbox.net.codec.login.LoginRequest

/**
 * Contains information required to process a [LoginRequest].
 *
 * @author Tom <rspsmods@gmail.com>
 */
data class LoginServiceRequest(val world: RSWorld, val login: LoginRequest)