package io.rsbox.engine.message

import io.rsbox.net.packet.DataOrder
import io.rsbox.net.packet.DataSignature
import io.rsbox.net.packet.DataTransformation
import io.rsbox.net.packet.DataType

/**
 * A [MessageValue] represents a single value which can be operated on throughout
 * a [Message]. A [Message] can hold multiple [MessageValue]s.
 *
 * @param id
 * A unique name that will be used to decode and encode the value.
 *
 * @author Tom <rspsmods@gmail.com>
 */
data class MessageValue(val id: String, val order: DataOrder, val transformation: DataTransformation, val type: DataType,
                        val signature: DataSignature)