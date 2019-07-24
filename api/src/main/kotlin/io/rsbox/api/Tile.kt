package io.rsbox.api

/**
 * Represents a tile in game
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

    /**
     * Checks to see if the params -> [Tile] is within a radius
     *
     * @param otherX the other [Tile] x coordinate
     * @param otherY the other [Tile] y coordinate
     * @param otherHeight other [Tile] height coordinate
     * @param radius The radius distance to check
     * @return [Boolean]
     */
    fun isWithinRadius(otherX: Int, otherZ: Int, otherHeight: Int, radius: Int): Boolean

    /**
     * Check to see if [other] is within a radius
     *
     * @param other other [Tile] object
     * @param radius the radius distance to check
     * @return [Boolean]
     */
    fun isWithinRadius(other: Tile, radius: Int): Boolean

    /**
     * Move [num] of tiles in a [Direction]
     *
     * @param direction [Direction] enum value
     * @param num The number of tiles to step
     * @return [Tile]
     */
    fun step(direction: Direction, num: Int = 1): Tile

    fun transform(x: Int, z: Int, height: Int): Tile

    fun transform(x: Int, z: Int): Tile

    fun transform(height: Int): Tile
}