package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class EventCameraPositionMessage(val pitch: Int, val yaw: Int) : Message