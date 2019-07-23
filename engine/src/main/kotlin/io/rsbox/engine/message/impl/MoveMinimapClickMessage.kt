package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class MoveMinimapClickMessage(val x: Int, val z: Int, val movementType: Int) : Message