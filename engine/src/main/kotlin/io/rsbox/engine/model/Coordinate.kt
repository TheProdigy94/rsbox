package io.rsbox.engine.model

/**
 * A coordinate is similar to a [RSTile], however it stores each vector value
 * separately.
 *
 * @author Tom <rspsmods@gmail.com>
 */
data class Coordinate(val x: Int, val z: Int, val height: Int) {

    /**
     * Returns the local tile of our region relative to the current [x] and [z].
     *
     * The [other] tile will always have coords equal to or greater than our own.
     */
    fun toLocal(other: RSTile): RSTile = RSTile(((other.x shr 3) - (x shr 3)) shl 3, ((other.z shr 3) - (z shr 3)) shl 3, height)

    val tile: RSTile get() = RSTile(x, z, height)
}