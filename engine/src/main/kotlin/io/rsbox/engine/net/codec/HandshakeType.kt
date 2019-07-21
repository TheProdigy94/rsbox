package io.rsbox.engine.net.codec

/**
 * @author Kyle Escobar
 */

enum class HandshakeType(val id: Int) {
    LOGIN(14),
    FILESTORE(15);

    companion object {
        val values = enumValues<HandshakeType>()
    }
}