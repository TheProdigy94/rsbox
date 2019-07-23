package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class LocAddChangeMessage(val id: Int, val settings: Int, val tile: Int) : Message