package io.rsbox.engine.fs.def

import io.rsbox.engine.fs.Definition
import io.netty.buffer.ByteBuf

/**
 * @author Tom <rspsmods@gmail.com>
 */
class VarpDef(override val id: Int) : Definition(id) {

    var configType = 0

    override fun decode(buf: ByteBuf, opcode: Int) {
        when (opcode) {
            5 -> configType = buf.readUnsignedShort()
        }
    }
}