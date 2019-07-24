package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpNpcTMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_COMPONENT_CHILD
import io.rsbox.api.INTERACTING_COMPONENT_PARENT
import io.rsbox.api.INTERACTING_NPC_ATTR
import io.rsbox.api.entity.Npc
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.entity.RSEntity
import io.rsbox.engine.model.priv.Privilege
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpNpcTHandler : MessageHandler<OpNpcTMessage> {

    override fun handle(client: Client, world: RSWorld, message: OpNpcTMessage) {
        val npc = world.npcs[message.npcIndex] ?: return
        val parent = message.componentHash shr 16
        val child = message.componentHash and 0xFFFF

        if (!client.lock.canNpcInteract()) {
            return
        }

        log(client, "Spell on npc: npc=%d. index=%d, component=[%d:%d], movement=%d", npc.id, message.npcIndex, parent, child, message.movementType)

        client.interruptQueues()
        client.resetInteractions()

        if (message.movementType == 1 && world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            client.moveTo(world.findRandomTileAround(npc.tile as RSTile, 1) ?: npc.tile as RSTile)
        }

        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        client.attr[INTERACTING_NPC_ATTR] = WeakReference(npc as Npc)
        client.attr[INTERACTING_COMPONENT_PARENT] = parent
        client.attr[INTERACTING_COMPONENT_CHILD] = child

        if (!world.plugins.executeSpellOnNpc(client, parent, child)) {
            client.writeMessage(RSEntity.NOTHING_INTERESTING_HAPPENS)
            if (world.devContext.debugMagicSpells) {
                client.writeMessage("Unhandled magic spell: [$parent, $child]")
            }
        }
    }
}