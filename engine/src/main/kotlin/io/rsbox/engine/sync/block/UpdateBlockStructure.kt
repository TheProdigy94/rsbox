package io.rsbox.engine.sync.block

import io.rsbox.engine.message.MessageValue

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class UpdateBlockStructure(val bit: Int, val values: List<MessageValue>)