package io.rsbox.engine.model

import com.google.common.base.MoreObjects
import io.rsbox.api.Direction
import io.rsbox.api.Tile
import io.rsbox.engine.model.region.Chunk
import io.rsbox.engine.model.region.ChunkCoords

/**
 * A 3D point in the world.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class RSTile : Tile {

    /**
     * A bit-packed integer that holds and represents the [x], [z] and [height] of the tile.
     */
    private val coordinate: Int

    override val x: Int get() = coordinate and 0x7FFF

    override val z: Int get() = (coordinate shr 15) and 0x7FFF

    override val height: Int get() = coordinate ushr 30

    val topLeftRegionX: Int get() = (x shr 3) - 6

    val topLeftRegionZ: Int get() = (z shr 3) - 6

    /**
     * Get the region id based on these coordinates.
     */
    override val regionId: Int get() = ((x shr 6) shl 8) or (z shr 6)

    /**
     * Returns the base tile of our region relative to the current [x], [z] and [Chunk.MAX_VIEWPORT].
     */
    val regionBase: RSTile get() = RSTile(((x shr 3) - (Chunk.MAX_VIEWPORT shr 4)) shl 3, ((z shr 3) - (Chunk.MAX_VIEWPORT shr 4)) shl 3, height)

    val chunkCoords: ChunkCoords get() = ChunkCoords.fromTile(this)

    /**
     * The tile packed as a 30-bit integer.
     */
    val as30BitInteger: Int get() = (z and 0x3FFF) or ((x and 0x3FFF) shl 14) or ((height and 0x3) shl 28)

    override val asTileHashMultiplier: Int get() = (z shr 13) or ((x shr 13) shl 8) or ((height and 0x3) shl 16)

    private constructor(coordinate: Int) {
        this.coordinate = coordinate
        check(height < TOTAL_HEIGHT_LEVELS) { "RSTile height level should not exceed maximum height! [height=$height]" }
    }

    constructor(x: Int, z: Int, height: Int = 0) : this((x and 0x7FFF) or ((z and 0x7FFF) shl 15) or (height shl 30))

    constructor(other: RSTile) : this(other.x, other.z, other.height)

    override fun transform(x: Int, z: Int, height: Int) = RSTile(this.x + x, this.z + z, this.height + height)

    override fun transform(x: Int, z: Int): Tile = RSTile(this.x + x, this.z + z, this.height)

    override fun transform(height: Int): Tile = RSTile(this.x, this.z, this.height + height)

    fun viewableFrom(other: RSTile, viewDistance: Int = 15): Boolean = getDistance(other) <= viewDistance

    override fun step(direction: Direction, num: Int): Tile = RSTile(this.x + (num * direction.getDeltaX()), this.z + (num * direction.getDeltaZ()), this.height)

    fun transformAndRotate(localX: Int, localZ: Int, orientation: Int, width: Int = 1, length: Int = 1): Tile {
        val localWidth = Chunk.CHUNK_SIZE - 1
        val localLength = Chunk.CHUNK_SIZE - 1

        return when (orientation) {
            0 -> transform(localX, localZ)
            1 -> transform(localZ, localLength - localX - (width - 1))
            2 -> transform(localWidth - localX - (width - 1), localLength - localZ - (length - 1))
            3 -> transform(localWidth - localZ - (length - 1), localX)
            else -> throw IllegalArgumentException("Illegal orientation! Value must be in bounds [0-3]. [orientation=$orientation]")
        }
    }

    override fun isWithinRadius(otherX: Int, otherZ: Int, otherHeight: Int, radius: Int): Boolean {
        if (otherHeight != height) {
            return false
        }
        val dx = Math.abs(x - otherX)
        val dz = Math.abs(z - otherZ)
        return dx <= radius && dz <= radius
    }

    /**
     * Checks if the [other] tile is within the [radius]x[radius] distance of
     * this [RSTile].
     *
     * @return true
     * if the tiles are on the same height and within radius of [radius] tiles.
     */
    override fun isWithinRadius(other: Tile, radius: Int): Boolean = isWithinRadius(other.x, other.z, other.height, radius)

    fun isInSameChunk(other: RSTile): Boolean = (x shr 3) == (other.x shr 3) && (z shr 3) == (other.z shr 3)

    fun getDistance(other: RSTile): Int {
        val dx = x - other.x
        val dz = z - other.z
        return Math.ceil(Math.sqrt((dx * dx + dz * dz).toDouble())).toInt()
    }

    fun getDelta(other: RSTile): Int = Math.abs(x - other.x) + Math.abs(z - other.z)

    /**
     * @return
     * The local tile of our region relative to the current [x] and [z].
     *
     * The [other] tile will always have coords equal to or greater than our own.
     */
    fun toLocal(other: RSTile): RSTile = RSTile(((other.x shr 3) - (x shr 3)) shl 3, ((other.z shr 3) - (z shr 3)) shl 3, height)

    /**
     * @return
     * A bit-packed value of the tile, in [Chunk] coordinates, which also stores
     * a rotation/orientation value.
     */
    fun toRotatedInteger(rot: Int): Int = ((height and 0x3) shl 24) or (((x shr 3) and 0x3FF) shl 14) or (((z shr 3) and 0x7FF) shl 3) or ((rot and 0x3) shl 1)

    /**
     * Checks if the [other] tile has the same coordinates as this tile.
     */
    override fun sameAs(other: Tile): Boolean = (other as RSTile).x == x && other.z == z && other.height == height

    override fun sameAs(x: Int, z: Int): Boolean = x == this.x && z == this.z

    override fun toString(): String = MoreObjects.toStringHelper(this).add("x", x).add("z", z).add("height", height).toString()

    override fun hashCode(): Int = coordinate

    override fun equals(other: Any?): Boolean {
        if (other is RSTile) {
            return other.coordinate == coordinate
        }
        return false
    }

    operator fun component1() = x

    operator fun component2() = z

    operator fun component3() = height

    operator fun minus(other: RSTile): RSTile = RSTile(x - other.x, z - other.z, height - other.height)

    operator fun plus(other: RSTile): RSTile = RSTile(x + other.x, z + other.z, height + other.height)

    companion object {
        /**
         * The total amount of height levels that can be used in the game.
         */
        const val TOTAL_HEIGHT_LEVELS = 4

        fun fromRotatedHash(packed: Int): RSTile {
            val x = ((packed shr 14) and 0x3FF) shl 3
            val z = ((packed shr 3) and 0x7FF) shl 3
            val height = (packed shr 28) and 0x3
            return RSTile(x, z, height)
        }

        fun from30BitHash(packed: Int): RSTile {
            val x = ((packed shr 14) and 0x3FFF)
            val z = ((packed) and 0x3FFF)
            val height = (packed shr 28)
            return RSTile(x, z, height)
        }

        fun fromRegion(region: Int): RSTile {
            val x = ((region shr 8) shl 6)
            val z = ((region and 0xFF) shl 6)
            return RSTile(x, z)
        }
    }
}