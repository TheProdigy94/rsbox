package io.rsbox.engine.message.handler

import io.rsbox.engine.action.PawnPathAction
import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpNpcUMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_ITEM
import io.rsbox.api.INTERACTING_ITEM_ID
import io.rsbox.api.INTERACTING_ITEM_SLOT
import io.rsbox.api.INTERACTING_NPC_ATTR
import io.rsbox.api.entity.Npc
import io.rsbox.api.item.Item
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.priv.Privilege
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpNpcUHandler : MessageHandler<OpNpcUMessage> {

    override fun handle(client: Client, world: RSWorld, message: OpNpcUMessage) {
        val index = message.npcIndex
        val npc = world.npcs[index] ?: return

        if (!client.lock.canNpcInteract() || !client.lock.canItemInteract()) {
            return
        }

        val movementType = message.movementType
        val itemId = message.item
        val itemSlot = message.slot

        val item = client.inventory[itemSlot] ?: return

        if (item.id != itemId) {
            return
        }

        log(client, "RSItem on npc: movement=%d, item=%s, slot=%d, npc=%s, index=%d", movementType, item, itemSlot, npc, index)

        if (movementType == 1 && world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            client.moveTo(world.findRandomTileAround(npc.tile as RSTile, 1) ?: npc.tile as RSTile)
        }

        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        client.attr[INTERACTING_NPC_ATTR] = WeakReference(npc as Npc)
        client.attr[INTERACTING_ITEM] = WeakReference(item as Item)
        client.attr[INTERACTING_ITEM_ID] = item.id
        client.attr[INTERACTING_ITEM_SLOT] = itemSlot
        client.executePlugin(PawnPathAction.itemUsePlugin)
    }
}