package io.rsbox.api.event.login

import io.rsbox.api.event.Event
import io.rsbox.api.entity.Player
import net.runelite.http.api.worlds.World

/**
 * @author Kyle Escobar
 */

class PlayerLoginEvent : Event() {
    fun getPlayer(): Player {
        return args[0] as Player
    }

    fun getWorld(): World {
        return args[1] as World
    }
}