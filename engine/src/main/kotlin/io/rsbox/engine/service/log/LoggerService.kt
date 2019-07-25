package io.rsbox.engine.service.log

import io.rsbox.engine.event.Event
import io.rsbox.engine.model.entity.RSClient
import io.rsbox.engine.model.entity.RSNpc
import io.rsbox.engine.model.entity.RSPawn
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.model.item.RSItem
import io.rsbox.engine.service.Service

/**
 * A [Service] responsible for logging in-game events when requested.
 *
 * Keep in mind that all methods are called from the game-thread. Any expensive
 * IO operation needing to be done should be queued independently from the
 * logger using the data provided by the logger.
 *
 * @author Tom <rspsmods@gmail.com>
 */
interface LoggerService : Service {

    fun logPacket(client: RSClient, message: String)

    fun logLogin(player: RSPlayer)

    fun logPublicChat(player: RSPlayer, message: String)

    fun logClanChat(player: RSPlayer, clan: String, message: String)

    fun logCommand(player: RSPlayer, command: String, vararg args: String)

    fun logItemDrop(player: RSPlayer, item: RSItem, slot: Int)

    fun logItemPickUp(player: RSPlayer, item: RSItem)

    fun logNpcKill(player: RSPlayer, npc: RSNpc)

    fun logPlayerKill(killer: RSPlayer, killed: RSPlayer)

    fun logEvent(pawn: RSPawn, event: Event)
}