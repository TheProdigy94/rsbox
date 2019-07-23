package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class IfButtonMessage(val hash: Int, val option: Int, val slot: Int, val item: Int) : Message