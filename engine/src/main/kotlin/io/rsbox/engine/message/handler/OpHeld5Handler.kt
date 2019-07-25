package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpHeld5Message
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_ITEM
import io.rsbox.api.INTERACTING_ITEM_ID
import io.rsbox.api.INTERACTING_ITEM_SLOT
import io.rsbox.api.item.Item
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.entity.RSClient
import io.rsbox.engine.model.entity.RSGroundItem
import io.rsbox.engine.model.item.RSItem
import io.rsbox.engine.service.log.LoggerService
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpHeld5Handler : MessageHandler<OpHeld5Message> {

    override fun handle(client: RSClient, world: RSWorld, message: OpHeld5Message) {
        if (!client.lock.canDropItems() || !client.canDropItems) {
            return
        }
        val hash = message.hash
        val slot = message.slot

        val item = client.inventory[slot] ?: return

        log(client, "Drop item: item=[%d, %d], slot=%d, interfaceId=%d, component=%d", item.id, item.amount, slot, hash shr 16, hash and 0xFFFF)

        client.attr[INTERACTING_ITEM] = WeakReference(item as Item)
        client.attr[INTERACTING_ITEM_ID] = item.id
        client.attr[INTERACTING_ITEM_SLOT] = slot

        client.resetFacePawn()

        if (world.plugins.canDropItem(client, item.id)) {
            val remove = client.inventory.remove(item, assureFullRemoval = false, beginSlot = slot)
            if (remove.completed > 0) {
                val floor = RSGroundItem(item.id, remove.completed, client.tile as RSTile, client)
                remove.firstOrNull()?.let { removed ->
                    floor.copyAttr(removed.item.attr)
                }
                world.spawn(floor)
                world.getService(LoggerService::class.java, searchSubclasses = true)?.logItemDrop(client, RSItem(item.id, remove.completed), slot)
            }
        }
    }
}