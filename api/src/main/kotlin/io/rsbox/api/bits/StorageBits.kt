package io.rsbox.api.bits

/**
 * Represents one, or multiple, bits that can be stored inside a [RSBitStorage].
 *
 * @author Tom <rspsmods@gmail.com>
 */
interface StorageBits {

    val startBit: Int

    val endBit: Int
}