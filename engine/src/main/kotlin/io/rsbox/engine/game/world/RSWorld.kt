package io.rsbox.engine.game.world

import io.rsbox.api.GameContext
import io.rsbox.api.World
import io.rsbox.util.codec.HuffmanCodec
import net.runelite.cache.IndexType
import net.runelite.cache.fs.Store

/**
 * @author Kyle Escobar
 */

class RSWorld(private val gameContext: GameContext) : World {
    /**
     * This is the storage for our cache.
     */
    internal lateinit var cacheStore: Store

    // TODO cache definitions and a shit ton after that... O.O

    /**
     * The [HuffmanCodec] used to compress / decompress public chat.
     */
    internal val huffman by lazy {
        val binary = cacheStore.getIndex(IndexType.BINARY)
        val archive = binary.findArchiveByName("huffman")!!
        val file = archive.getFiles(cacheStore.storage.loadArchive(archive)!!).files[0]
            HuffmanCodec(file.contents)
    }

    /**
     * Current Cycle count
     */
    internal var currentCycle = 0

    /**
     * Reboot timer
     */
    internal var rebootTimer = -1

    internal fun init() {

    }

    internal fun preLoad() {

    }

    internal fun postLoad() {

    }
}