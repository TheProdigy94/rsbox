package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class OpHeldTMessage(val fromComponentHash: Int, val toComponentHash: Int, val spellSlot: Int, val itemSlot: Int, val item: Int) : Message