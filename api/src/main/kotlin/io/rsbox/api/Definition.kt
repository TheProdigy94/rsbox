package io.rsbox.api

import io.netty.buffer.ByteBuf
import io.rsbox.util.io.BufferUtils.readString
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

/**
 * @author Kyle Escobar
 */

abstract class Definition(open val id: Int) {
    fun decode(buf: ByteBuf) {
        while(true) {
            val opcode = buf.readUnsignedByte().toInt()
            if(opcode == 0) {
                break
            }
            decode(buf, opcode)
        }
    }

    abstract fun decode(buf: ByteBuf, opcode: Int)

    fun readParams(buf: ByteBuf): Int2ObjectOpenHashMap<Any> {
        val map = Int2ObjectOpenHashMap<Any>()
        val length = buf.readUnsignedShort()

        for(i in 0..length) {
            val isString = buf.readUnsignedByte().toInt() == 1
            val id = buf.readUnsignedMedium()

            if(isString) {
                map[id] = buf.readString()
            } else {
                map[id] = buf.readInt()
            }
        }
        return map
    }
}