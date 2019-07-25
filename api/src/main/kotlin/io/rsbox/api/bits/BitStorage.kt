package io.rsbox.api.bits

import io.rsbox.api.AttributeKey
import io.rsbox.api.entity.Pawn
import io.rsbox.util.BitManipulation

/**
 * A storage for up to thirty-two (32) bits. This means you can have up to 32
 * possible values stored in a single integer instead of say, 32 separate booleans.
 *
 * @param key
 * The attribute key used to store the packed bit values for this storage.
 *
 * If the given [AttributeKey] is persistent, the bits in this storage will
 * be persistent.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class BitStorage(val key: AttributeKey<Int>) {

    /**
     * Constructs a new [BitStorage] with a persistent [AttributeKey], which means
     * that any modification done per player will be persistent for said player.
     */
    constructor(persistenceKey: String) : this(AttributeKey<Int>(persistenceKey))

    /**
     * Get the unpacked value of the bits in range of [StorageBits.startBit]
     * to [StorageBits.endBit].
     */
    fun get(p: Pawn, bits: StorageBits): Int = BitManipulation.getBit(packed = getPackedBits(p), startBit = bits.startBit, endBit = bits.endBit)

    /**
     * Sets the unpacked value of the bits in range of [StorageBits.startBit]
     * to [StorageBits.endBit], to [value].
     */
    fun set(p: Pawn, bits: StorageBits, value: Int) {
        set(p, BitManipulation.setBit(packed = getPackedBits(p), startBit = bits.startBit, endBit = bits.endBit, value = value))
    }

    private fun getPackedBits(p: Pawn): Int = p.getAttributes()[key] ?: 0

    private fun set(p: Pawn, packed: Int) {
        p.getAttributes()[key] = packed
    }
}