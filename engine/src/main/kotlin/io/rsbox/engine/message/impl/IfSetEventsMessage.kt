package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class IfSetEventsMessage(val hash: Int, val fromChild: Int, val toChild: Int, val setting: Int) : Message