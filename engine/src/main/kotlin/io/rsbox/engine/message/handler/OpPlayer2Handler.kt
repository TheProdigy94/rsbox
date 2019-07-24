package io.rsbox.engine.message.handler

import io.rsbox.engine.action.PawnPathAction
import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpPlayer2Message
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_OPT_ATTR
import io.rsbox.api.INTERACTING_PLAYER_ATTR
import io.rsbox.api.entity.Player
import io.rsbox.engine.model.entity.Client
import java.lang.ref.WeakReference

/**
 * @author Triston Plummer ("Dread")
 */
class OpPlayer2Handler : MessageHandler<OpPlayer2Message> {

    override fun handle(client: Client, world: RSWorld, message: OpPlayer2Message) {
        val index = message.index
        // The interaction option id.
        val option = 2
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