package io.rsbox.engine.service.log

import io.rsbox.engine.event.Event
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.entity.Npc
import io.rsbox.engine.model.entity.Pawn
import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.model.item.Item
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

    fun logPacket(client: Client, message: String)

    fun logLogin(player: Player)

    fun logPublicChat(player: Player, message: String)

    fun logClanChat(player: Player, clan: String, message: String)

    fun logCommand(player: Player, command: String, vararg args: String)

    fun logItemDrop(player: Player, item: Item, slot: Int)

    fun logItemPickUp(player: Player, item: Item)

    fun logNpcKill(player: Player, npc: Npc)

    fun logPlayerKill(killer: Player, killed: Player)

    fun logEvent(pawn: Pawn, event: Event)
}