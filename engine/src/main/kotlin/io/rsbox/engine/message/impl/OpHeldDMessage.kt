package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class OpHeldDMessage(val srcSlot: Int, val dstSlot: Int, val componentHash: Int, val insertMode: Boolean) : Message