package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class ObjCountMessage(val item: Int, val oldAmount: Int, val newAmount: Int, val tile: Int) : Message