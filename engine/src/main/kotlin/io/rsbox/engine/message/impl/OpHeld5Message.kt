package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class OpHeld5Message(val hash: Int, val slot: Int, val item: Int) : Message