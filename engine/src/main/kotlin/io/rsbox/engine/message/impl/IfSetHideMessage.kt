package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class IfSetHideMessage(val hash: Int, val hidden: Boolean) : Message