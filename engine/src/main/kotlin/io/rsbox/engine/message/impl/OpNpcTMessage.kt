package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class OpNpcTMessage(val npcIndex: Int, val componentHash: Int, val componentSlot: Int, val movementType: Int) : Message