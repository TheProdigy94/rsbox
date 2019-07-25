package io.rsbox.api.event.inter

import io.rsbox.api.entity.Player
import io.rsbox.api.event.Cancellable
import io.rsbox.api.event.Event

/**
 * @author Kyle Escobar
 */

class ButtonClickEvent(val player: Player, val parent: Int, val child: Int) : Event(), Cancellable {
    private var cancelEvent = false

    val option: Int = player.getInteractingOption()

    val subComponent: Int = player.getInteractingSlot()

    override fun setCancelled(cancel: Boolean) {
        this.cancelEvent = cancel
    }

    override fun isCancelled(): Boolean {
        return this.cancelEvent
    }
}