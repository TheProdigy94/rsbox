package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Triston Plummer ("Dread")
 *
 * Represents a message that defines a player's right click option
 *
 * @param option the option text
 * @param index the index of the option
 * @param leftClick if this option should be the default left-click option
 */
data class SetOpPlayerMessage(val option: String, val index: Int, val leftClick: Boolean) : Message