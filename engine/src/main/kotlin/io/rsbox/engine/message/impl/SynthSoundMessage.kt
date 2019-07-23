package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class SynthSoundMessage(val sound: Int, val volume: Int, val delay: Int) : Message