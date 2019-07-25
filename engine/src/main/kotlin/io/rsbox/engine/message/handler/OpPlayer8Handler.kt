package io.rsbox.engine.message.handler

import io.rsbox.engine.action.PawnPathAction
import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpPlayer8Message
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_OPT_ATTR
import io.rsbox.api.INTERACTING_PLAYER_ATTR
import io.rsbox.api.entity.Player
import io.rsbox.engine.model.entity.RSClient
import java.lang.ref.WeakReference

/**
 * @author Triston Plummer ("Dread")
 */
class OpPlayer8Handler : MessageHandler<OpPlayer8Message> {

    override fun handle(client: RSClient, world: RSWorld, message: OpPlayer8Message) {
        val index = message.index
        // The interaction option id.
        val option = 8
        // The index of the option in the player's option array.
        val optionIndex = option - 1

        if (!client.lock.canPlayerInteract()) {
            return
        }

        val other = world.players[index] ?: return

        if (client.options[optionIndex] == null || other == client) {
            return
        }

        log(client, "RSPlayer option: name=%s, opt=%d", other.username, option)

        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        client.attr[INTERACTING_PLAYER_ATTR] = WeakReference(other as Player)
        client.attr[INTERACTING_OPT_ATTR] = option
        client.executePlugin(PawnPathAction.walkPlugin)
    }
}