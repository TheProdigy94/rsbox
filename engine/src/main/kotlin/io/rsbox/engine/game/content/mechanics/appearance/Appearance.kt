package io.rsbox.engine.game.content.mechanics.appearance

import io.rsbox.api.NEW_ACCOUNT_ATTR
import io.rsbox.api.TaskPriority
import io.rsbox.api.UpdateBlockType
import io.rsbox.engine.model.entity.RSPlayer

/**
 * @author Kyle Escobar
 */

class Appearance(val player: RSPlayer) {
    fun openAppearanceSelection() {
        if(player.attr[NEW_ACCOUNT_ATTR] == true) {
            player.queue(TaskPriority.WEAK) {
                player.lock()
                val appearance = selectAppearance() ?: return@queue
                player.unlock()
                player.setAppearance(appearance)
                player.addBlock(UpdateBlockType.APPEARANCE)
            }
        }
    }
}