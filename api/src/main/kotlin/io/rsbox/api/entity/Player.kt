package io.rsbox.api.entity

import io.rsbox.api.Appearance
import io.rsbox.api.entity.Pawn


/**
 * Object which represents a player
 *
 * @author Kyle Escobar
 */
interface Player : Pawn {
    /**
     * The display name of the player.
     * NOT the login name.
     */
    var username: String

    /**
     * The appearance of the player
     */
    fun getAppearance(): Appearance

    /**
     *
     */
}