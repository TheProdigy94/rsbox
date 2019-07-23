package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Triston Plummer ("Dread")
 *
 * Represents the fifth player interaction option
 *
 * @param index The index of the player that the client is interacting with
 */
class OpPlayer5Message(val index: Int) : Message