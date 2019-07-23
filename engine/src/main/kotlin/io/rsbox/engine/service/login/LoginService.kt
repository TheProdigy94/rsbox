package io.rsbox.engine.service.login

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.rsbox.engine.RSServer
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.protocol.GameHandler
import io.rsbox.engine.protocol.GameMessageEncoder
import io.rsbox.engine.protocol.PacketMetadata
import io.rsbox.engine.service.GameService
import io.rsbox.engine.service.Service
import io.rsbox.engine.service.rsa.RsaService
import io.rsbox.engine.service.serializer.PlayerSerializerService
import io.rsbox.engine.service.world.SimpleWorldVerificationService
import io.rsbox.engine.service.world.WorldVerificationService
import io.rsbox.engine.system.GameSystem
import io.rsbox.net.codec.game.GamePacketDecoder
import io.rsbox.net.codec.game.GamePacketEncoder
import io.rsbox.net.codec.login.LoginRequest
import io.rsbox.util.ServerProperties
import io.rsbox.util.io.IsaacRandom
import mu.KLogging
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

/**
 * A [Service] that is responsible for handling incoming login requests.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class LoginService : Service {

    /**
     * The [PlayerSerializerService] implementation that will be used to decode
     * and encode the player data.
     */
    lateinit var serializer: PlayerSerializerService

    /**
     * The [LoginServiceRequest] requests that will be handled by our workers.
     */
    val requests = LinkedBlockingQueue<LoginServiceRequest>()

    private var threadCount = 1

    override fun init(server: RSServer, world: RSWorld, serviceProperties: ServerProperties) {
        threadCount = serviceProperties.getOrDefault("thread-count", 3)
    }

    override fun postLoad(server: RSServer, world: RSWorld) {
        serializer = world.getService(PlayerSerializerService::class.java, searchSubclasses = true)!!

        val worldVerificationService = world.getService(WorldVerificationService::class.java, searchSubclasses = true) ?: SimpleWorldVerificationService()

        val executorService = Executors.newFixedThreadPool(threadCount, ThreadFactoryBuilder().setNameFormat("login-worker").setUncaughtExceptionHandler { t, e -> logger.error("Error with thread $t", e) }.build())
        for (i in 0 until threadCount) {
            executorService.execute(LoginWorker(this, worldVerificationService))
        }
    }

    override fun bindNet(server: RSServer, world: RSWorld) {
    }

    override fun terminate(server: RSServer, world: RSWorld) {
    }

    fun addLoginRequest(world: RSWorld, request: LoginRequest) {
        val serviceRequest = LoginServiceRequest(world, request)
        requests.offer(serviceRequest)
    }

    fun successfulLogin(client: Client, world: RSWorld, encodeRandom: IsaacRandom, decodeRandom: IsaacRandom) {
        val gameSystem = GameSystem(
                channel = client.channel, world = world, client = client,
                service = client.world.getService(GameService::class.java)!!)

        client.gameSystem = gameSystem
        client.channel.attr(GameHandler.SYSTEM_KEY).set(gameSystem)

        /*
         * NOTE(Tom): we should be able to use an parallel task to handle
         * the pipeline work and then schedule for the [client] to log in on the
         * next game cycle after completion. Should benchmark first.
         */
        val pipeline = client.channel.pipeline()
        val isaacEncryption = client.world.getService(RsaService::class.java) != null
        val encoderIsaac = if (isaacEncryption) encodeRandom else null
        val decoderIsaac = if (isaacEncryption) decodeRandom else null

        if (client.channel.isActive) {
            pipeline.remove("handshake_encoder")
            pipeline.remove("login_decoder")
            pipeline.remove("login_encoder")

            pipeline.addFirst("packet_encoder", GamePacketEncoder(encoderIsaac))
            pipeline.addAfter("packet_encoder", "message_encoder", GameMessageEncoder(gameSystem.service.messageEncoders, gameSystem.service.messageStructures))

            pipeline.addBefore("handler", "packet_decoder",
                    GamePacketDecoder(decoderIsaac, PacketMetadata(gameSystem.service.messageStructures)))

            client.login()
            client.channel.flush()
        }
    }

    companion object : KLogging()
}