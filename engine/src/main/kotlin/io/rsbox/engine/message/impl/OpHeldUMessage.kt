package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class OpHeldUMessage(val fromComponentHash: Int, val fromSlot: Int, val fromItem: Int,
                          val toComponentHash: Int, val toSlot: Int, val toItem: Int) : Message