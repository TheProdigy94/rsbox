package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class IfSetAnimMessage(val hash: Int, val anim: Int) : Message