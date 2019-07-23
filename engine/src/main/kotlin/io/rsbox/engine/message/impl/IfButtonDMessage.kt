package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class IfButtonDMessage(val srcComponentHash: Int, val srcSlot: Int, val srcItem: Int,
                            val dstComponentHash: Int, val dstSlot: Int, val dstItem: Int) : Message