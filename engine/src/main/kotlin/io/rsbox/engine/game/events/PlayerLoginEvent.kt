package io.rsbox.engine.game.events

import io.rsbox.engine.game.content.mechanics.appearance.Appearance
import io.rsbox.engine.model.entity.RSPlayer

/**
 * @author Kyle Escobar
 */

object PlayerLoginEvent {
    fun execute(player: RSPlayer) {
        Appearance(player).openAppearanceSelection()
    }
}