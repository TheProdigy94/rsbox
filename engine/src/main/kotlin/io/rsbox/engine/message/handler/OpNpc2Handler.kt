package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpNpc2Message
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSClient
import io.rsbox.engine.model.priv.Privilege

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpNpc2Handler : MessageHandler<OpNpc2Message> {

    override fun handle(client: RSClient, world: RSWorld, message: OpNpc2Message) {
        val npc = world.npcs[message.index] ?: return

        if (!client.lock.canAttack()) {
            return
        }

        log(client, "RSNpc option 2: index=%d, movement=%d, npc=%s", message.index, message.movementType, npc)

        if (message.movementType == 1 && world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            client.moveTo(world.findRandomTileAround(npc.tile as RSTile, 1) ?: npc.tile as RSTile)
        }

        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        client.attack(npc)
    }
}