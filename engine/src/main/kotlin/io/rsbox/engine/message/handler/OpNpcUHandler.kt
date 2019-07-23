package io.rsbox.engine.message.handler

import io.rsbox.engine.action.PawnPathAction
import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpNpcUMessage
import io.rsbox.engine.model.World
import io.rsbox.engine.model.attr.INTERACTING_ITEM
import io.rsbox.engine.model.attr.INTERACTING_ITEM_ID
import io.rsbox.engine.model.attr.INTERACTING_ITEM_SLOT
import io.rsbox.engine.model.attr.INTERACTING_NPC_ATTR
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.priv.Privilege
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpNpcUHandler : MessageHandler<OpNpcUMessage> {

    override fun handle(client: Client, world: World, message: OpNpcUMessage) {
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

        log(client, "Item on npc: movement=%d, item=%s, slot=%d, npc=%s, index=%d", movementType, item, itemSlot, npc, index)

        if (movementType == 1 && world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            client.moveTo(world.findRandomTileAround(npc.tile, 1) ?: npc.tile)
        }

        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        client.attr[INTERACTING_NPC_ATTR] = WeakReference(npc)
        client.attr[INTERACTING_ITEM] = WeakReference(item)
        client.attr[INTERACTING_ITEM_ID] = item.id
        client.attr[INTERACTING_ITEM_SLOT] = itemSlot
        client.executePlugin(PawnPathAction.itemUsePlugin)
    }
}