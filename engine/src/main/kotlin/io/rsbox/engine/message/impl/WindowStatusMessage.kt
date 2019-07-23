package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class WindowStatusMessage(val mode: Int, val width: Int, val height: Int) : Message