package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class UpdateStatMessage(val skill: Int, val level: Int, val xp: Int) : Message