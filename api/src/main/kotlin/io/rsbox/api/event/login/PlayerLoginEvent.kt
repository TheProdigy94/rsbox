package io.rsbox.api.event.login

import io.rsbox.api.World
import io.rsbox.api.event.Event
import io.rsbox.api.entity.Player

/**
 * @author Kyle Escobar
 */

class PlayerLoginEvent(val player: Player, val world: World) : Event()