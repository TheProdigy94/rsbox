package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class IfSetNpcHeadMessage(val hash: Int, val npc: Int) : Message