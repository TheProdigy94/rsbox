package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class MessageGameMessage(val type: Int, val username: String?, val message: String) : Message