package io.rsbox.api.event.inter

import io.rsbox.api.entity.Player
import io.rsbox.api.event.Cancellable
import io.rsbox.api.event.Event

/**
 * @author Kyle Escobar
 */

class ButtonClickEvent : Event(), Cancellable {
    private var cancelEvent = false

    // Args
    private var player: Player? = null
    private var parent: Int = -1
    private var child: Int = -1
    override fun init(vararg objArgs: Any?) {
        player = args[0] as Player
        parent = args[1] as Int
        child = args[2] as Int
    }

    fun getPlayer(): Player {
        return this.player!!
    }

    fun getParent(): Int {
        return this.parent
    }

    fun getChild(): Int {
        return this.child
    }

    fun getOption(): Int {
        return getPlayer().getInteractingOption()
    }

    fun getSubComponentId(): Int {
        return getPlayer().getInteractingSlot()
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelEvent = cancel
    }

    override fun isCancelled(): Boolean {
        return this.cancelEvent
    }
}