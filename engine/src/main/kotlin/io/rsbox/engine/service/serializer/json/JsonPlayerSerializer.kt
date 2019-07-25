package io.rsbox.engine.service.serializer.json

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.rsbox.engine.RSServer
import io.rsbox.engine.model.*
import io.rsbox.api.AttributeKey
import io.rsbox.engine.model.container.ItemContainer
import io.rsbox.engine.model.entity.RSClient
import io.rsbox.api.DisplayMode
import io.rsbox.engine.model.item.RSItem
import io.rsbox.engine.model.priv.Privilege
import io.rsbox.engine.model.timer.TimerKey
import io.rsbox.engine.service.serializer.PlayerLoadResult
import io.rsbox.engine.service.serializer.PlayerSerializerService
import io.rsbox.net.codec.login.LoginRequest
import io.rsbox.util.ServerProperties
import mu.KLogging
import org.mindrot.jbcrypt.BCrypt
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Arrays

class JsonPlayerSerializer : PlayerSerializerService() {

    private lateinit var path: Path

    override fun initSerializer(server: RSServer, world: RSWorld, serviceProperties: ServerProperties) {
        path = Paths.get(serviceProperties.getOrDefault("path", "./rsbox/data/saves/"))
        if (!Files.exists(path)) {
            Files.createDirectory(path)
            logger.info("Path does not exist: $path, creating directory...")
        }
    }

    override fun loadClientData(client: RSClient, request: LoginRequest): PlayerLoadResult {
        val save = path.resolve(client.loginUsername)
        if (!Files.exists(save)) {
            configureNewPlayer(client, request)
            client.uid = PlayerUID(client.loginUsername)
            saveClientData(client)
            return PlayerLoadResult.NEW_ACCOUNT
        }
        try {
            val world = client.world
            val reader = Files.newBufferedReader(save)
            val json = Gson()
            val data = json.fromJson<JsonPlayerSaveData>(reader, JsonPlayerSaveData::class.java)
            reader.close()

            if (!request.reconnecting) {
                /*
                 * If the [request] is not a [LoginRequest.reconnecting] request, we have to
                 * verify the password is correct.
                 */
                if (!BCrypt.checkpw(request.password, data.passwordHash)) {
                    return PlayerLoadResult.INVALID_CREDENTIALS
                }
            } else {
                /*
                 * If the [request] is a [LoginRequest.reconnecting] request, we
                 * verify that the login xteas match from our previous session.
                 */
                if (!Arrays.equals(data.previousXteas, request.xteaKeys)) {
                    return PlayerLoadResult.INVALID_RECONNECTION
                }
            }

            client.loginUsername = data.username
            client.uid = PlayerUID(data.username)
            client.username = data.displayName
            client.passwordHash = data.passwordHash
            client.tile = RSTile(data.x, data.z, data.height)
            client.privilege = world.privileges.get(data.privilege) ?: Privilege.DEFAULT
            client.runEnergy = data.runEnergy
            client.interfaces.displayMode = DisplayMode.values.firstOrNull { it.id == data.displayMode } ?: DisplayMode.FIXED
            client.appearance = RSAppearance(data.appearance.looks, data.appearance.colors, RSGender.values.firstOrNull { it.id == data.appearance.gender } ?: RSGender.MALE)
            data.skills.forEach { skill ->
                client.getSkills().setXp(skill.skill, skill.xp)
                client.getSkills().setCurrentLevel(skill.skill, skill.lvl)
            }
            data.itemContainers.forEach {
                val key = world.plugins.containerKeys.firstOrNull { other -> other.name == it.name }
                if (key == null) {
                    logger.error { "Container was found in serialized data, but is not registered to our RSWorld. [key=${it.name}]" }
                    return@forEach
                }
                val container = if (client.containers.containsKey(key)) client.containers[key] else {
                    client.containers[key] = ItemContainer(client.world.definitions, key)
                    client.containers[key]
                }!!
                it.items.forEach { slot, item ->
                    container[slot] = item
                }
            }
            data.attributes.forEach { (key, value) ->
                val attribute = AttributeKey<Any>(key)
                client.attr[attribute] = if (value is Double) value.toInt() else value
            }
            data.timers.forEach { timer ->
                var time = timer.timeLeft
                if (timer.tickOffline) {
                    val elapsed = System.currentTimeMillis() - timer.currentMs
                    val ticks = (elapsed / client.world.gameContext.cycleTime).toInt()
                    time -= ticks
                }
                val key = TimerKey(timer.identifier, timer.tickOffline)
                client.timers[key] = Math.max(0, time)
            }
            data.varps.forEach { varp ->
                client.varps.setState(varp.id, varp.state)
            }

            return PlayerLoadResult.LOAD_ACCOUNT
        } catch (e: Exception) {
            logger.error(e) { "Error when loading player: ${request.username}" }
            return PlayerLoadResult.MALFORMED
        }
    }

    override fun saveClientData(client: RSClient): Boolean {
        val data = JsonPlayerSaveData(passwordHash = client.passwordHash, username = client.loginUsername, previousXteas = client.currentXteaKeys,
            displayName = client.username, x = client.tile.x, z = client.tile.z, height = client.tile.height,
            privilege = client.privilege.id, runEnergy = client.runEnergy, displayMode = client.interfaces.displayMode.id,
            appearance = client.getPersistentAppearance(), skills = client.getPersistentSkills(), itemContainers = client.getPersistentContainers(),
            attributes = client.attr.toPersistentMap(), timers = client.timers.toPersistentTimers(),
            varps = client.varps.getAll().filter { it.state != 0 })
        val writer = Files.newBufferedWriter(path.resolve(client.loginUsername))
        val json = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        json.toJson(data, writer)
        writer.close()
        return true
    }

    private fun RSClient.getPersistentContainers(): List<PersistentContainer> {
        val persistent = mutableListOf<PersistentContainer>()

        containers.forEach { (key, container) ->
            if (!container.isEmpty) {
                persistent.add(PersistentContainer(key.name, container.toMap()))
            }
        }

        return persistent
    }

    private fun RSClient.getPersistentSkills(): List<PersistentSkill> {
        val skills = mutableListOf<PersistentSkill>()

        for (i in 0 until getSkills().maxSkills) {
            val xp = getSkills().getCurrentXp(i)
            val lvl = getSkills().getCurrentLevel(i)

            skills.add(PersistentSkill(skill = i, xp = xp, lvl = lvl))
        }

        return skills
    }

    private fun RSClient.getPersistentAppearance(): PersistentAppearance = PersistentAppearance(appearance.gender.id, appearance.looks, appearance.colors)

    data class PersistentAppearance(@JsonProperty("gender") val gender: Int,
                                    @JsonProperty("looks") val looks: IntArray,
                                    @JsonProperty("colors") val colors: IntArray)

    data class PersistentContainer(@JsonProperty("name") val name: String,
                                   @JsonProperty("items") val items: Map<Int, RSItem>)

    data class PersistentSkill(@JsonProperty("skill") val skill: Int,
                               @JsonProperty("xp") val xp: Double,
                               @JsonProperty("lvl") val lvl: Int)

    companion object : KLogging()
}