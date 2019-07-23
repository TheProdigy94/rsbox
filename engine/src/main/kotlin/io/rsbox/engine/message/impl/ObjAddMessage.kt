package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class ObjAddMessage(val item: Int, val amount: Int, val tile: Int) : Message