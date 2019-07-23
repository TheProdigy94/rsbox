package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class IfSetObjectMessage(val hash: Int, val item: Int, val amount: Int) : Message