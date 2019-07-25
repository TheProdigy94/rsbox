package io.rsbox.engine.game.content.mechanics.appearance

import io.rsbox.api.NEW_ACCOUNT_ATTR
import io.rsbox.api.TaskPriority
import io.rsbox.api.UpdateBlockType
import io.rsbox.engine.model.RSAppearance
import io.rsbox.engine.model.entity.RSPlayer

/**
 * @author Kyle Escobar
 */

object Appearance {
    fun openAppearanceSelection(player: RSPlayer) {
        if(true) {
            player.queue(TaskPriority.STRONG) {
                val appearance = selectAppearance() ?: return@queue
                player.appearance = appearance as RSAppearance
                player.addBlock(UpdateBlockType.APPEARANCE)
            }
        }
    }
}