package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class OpHeld2Message(val item: Int, val slot: Int, val componentHash: Int) : Message