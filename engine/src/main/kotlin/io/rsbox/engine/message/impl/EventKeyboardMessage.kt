package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class EventKeyboardMessage(val events: List<KeyEvent>) : Message {

    data class KeyEvent(val key: Int, val lastKeyPress: Int)
}