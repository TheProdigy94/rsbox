package io.rsbox.api.entity

import io.rsbox.api.AttributeMap

/**
 * @author Kyle Escobar
 */

interface Pawn : Entity {
    /**
     * Returns the pawn ATTR map
     */
    fun getAttributes(): AttributeMap

    /**
     * Create a queue on the [Pawn]
     * @param priority Task priority
     * @param logic [Unit] lamda logic
     */
}