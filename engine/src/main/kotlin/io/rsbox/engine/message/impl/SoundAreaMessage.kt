package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class SoundAreaMessage(val tileHash: Int, val id: Int, val radius: Int, val volume: Int, val delay: Int) : Message