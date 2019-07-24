package io.rsbox.engine.message.handler

import io.rsbox.engine.action.PawnPathAction
import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpNpc3Message
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_NPC_ATTR
import io.rsbox.api.INTERACTING_OPT_ATTR
import io.rsbox.api.entity.Npc
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.priv.Privilege
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpNpc3Handler : MessageHandler<OpNpc3Message> {

    override fun handle(client: Client, world: RSWorld, message: OpNpc3Message) {
        val npc = world.npcs[message.index] ?: return

        if (!client.lock.canNpcInteract()) {
            return
        }

        log(client, "RSNpc option 3: index=%d, movement=%d, npc=%s", message.index, message.movementType, npc)

        if (message.movementType == 1 && world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            client.moveTo(world.findRandomTileAround(npc.tile as RSTile, 1) ?: npc.tile as RSTile)
        }

        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        client.attr[INTERACTING_OPT_ATTR] = 3
        client.attr[INTERACTING_NPC_ATTR] = WeakReference(npc as Npc)
        client.executePlugin(PawnPathAction.walkPlugin)
    }
}