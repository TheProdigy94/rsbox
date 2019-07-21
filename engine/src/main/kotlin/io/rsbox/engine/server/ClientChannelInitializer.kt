package io.rsbox.engine.server

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.IdleStateHandler
import io.netty.handler.traffic.ChannelTrafficShapingHandler
import io.netty.handler.traffic.GlobalTrafficShapingHandler
import io.rsbox.engine.game.world.RSWorld
import io.rsbox.engine.server.handlers.GameHandler
import net.runelite.cache.fs.Store
import java.math.BigInteger
import java.util.concurrent.Executors

/**
 * @author Kyle Escobar
 */

class ClientChannelInitializer(
    private val revision: Int,
    private val rsaExponent: BigInteger?,
    private val rsaModulus: BigInteger?,
    private val cacheStore: Store,
    private val world: RSWorld
    ) : ChannelInitializer<SocketChannel>() {
    /**
     * A global traffic handler that limits the amount of bandwidth all channels
     * can take up at once.
     */
    private val globalTrafficHandler = GlobalTrafficShapingHandler(Executors.newSingleThreadScheduledExecutor(), 0, 0, 1000)

    private val handler = GameHandler(cacheStore, world)

    override fun initChannel(ch: SocketChannel) {
        val p = ch.pipeline()
        val crcs = cacheStore.indexes.map { it.crc }.toIntArray()

        p.addLast("global_traffic", globalTrafficHandler)
        p.addLast("channel_traffic", ChannelTrafficShapingHandler(0, 1024 * 5, 1000))
        p.addLast("timeout", IdleStateHandler(30, 0, 0))
        p.addLast("handshake_encoder", HandshakeEncoder())
        p.addLast("handshake_decoder", HandshakeDecoder(revision = revision, cacheCrcs = crcs, rsaExponent = rsaExponent, rsaModulus = rsaModulus))
        p.addLast("handler", handler)
    }
}