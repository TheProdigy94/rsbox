package io.rsbox.api

/**
 * @author Kyle Escobar
 */

interface Tile {
    fun sameAs(other: Tile): Boolean

    fun sameAs(x: Int, z: Int): Boolean

    val regionId: Int

    val asTileHashMultiplier: Int

    val x: Int
    val z: Int
    val height: Int
}