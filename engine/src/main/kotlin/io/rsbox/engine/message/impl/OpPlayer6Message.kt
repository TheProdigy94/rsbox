package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Triston Plummer ("Dread")
 *
 * Represents the sixth player interaction option
 *
 * @param index The index of the player that the client is interacting with
 */
class OpPlayer6Message(val index: Int) : Message