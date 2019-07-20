package io.rsbox.engine

import io.rsbox.api.GameContext

/**
 * @author Kyle Escobar
 */

class RSGameContext(
    private val name: String,
    private val revision: Int,
    private val tickSpeed: Int,
    private val maxPlayers: Int
) : GameContext {
    override fun getName(): String {
        return name
    }

    override fun getRevision(): Int {
        return revision
    }

    override fun getTickSpeed(): Int {
        return tickSpeed
    }

    override fun getMaxPlayers(): Int {
        return maxPlayers
    }

}