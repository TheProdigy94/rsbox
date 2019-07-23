package io.rsbox.engine.action

import io.rsbox.engine.fs.def.ItemDef
import io.rsbox.engine.message.impl.SetMapFlagMessage
import io.rsbox.engine.model.MovementQueue
import io.rsbox.engine.model.attr.GROUNDITEM_PICKUP_TRANSACTION
import io.rsbox.engine.model.attr.INTERACTING_GROUNDITEM_ATTR
import io.rsbox.engine.model.attr.INTERACTING_ITEM
import io.rsbox.engine.model.attr.INTERACTING_OPT_ATTR
import io.rsbox.engine.model.entity.Entity
import io.rsbox.engine.model.entity.GroundItem
import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.model.item.Item
import io.rsbox.engine.model.queue.QueueTask
import io.rsbox.engine.model.queue.TaskPriority
import io.rsbox.engine.oldplugin.Plugin
import io.rsbox.engine.service.log.LoggerService
import java.lang.ref.WeakReference

/**
 * This class is responsible for moving towards a [GroundItem].
 *
 * @author Tom <rspsmods@gmail.com>
 */
object GroundItemPathAction {

    /**
     * The option used to specify that a walk action should execute item on
     * ground item plugins when destination is reached.
     */
    internal const val ITEM_ON_GROUND_ITEM_OPTION = -1

    val walkPlugin: Plugin.() -> Unit = {
        val p = ctx as Player
        val item = p.attr[INTERACTING_GROUNDITEM_ATTR]!!.get()!!
        val opt = p.attr[INTERACTING_OPT_ATTR]!!

        if (p.tile.sameAs(item.tile)) {
            handleAction(p, item, opt)
        } else {
            p.walkTo(item.tile, MovementQueue.StepType.NORMAL)
            p.queue(TaskPriority.STANDARD) {
                terminateAction = {
                    p.stopMovement()
                    p.write(SetMapFlagMessage(255, 255))
                }
                awaitArrival(item, opt)
            }
        }
    }

    private suspend fun QueueTask.awaitArrival(item: GroundItem, opt: Int) {
        val p = ctx as Player
        val destination = p.movementQueue.peekLast()
        if (destination == null) {
            p.writeMessage(Entity.YOU_CANT_REACH_THAT)
            return
        }
        while (true) {
            if (!p.tile.sameAs(destination)) {
                wait(1)
                continue
            }
            // TODO: check if player can grab the item by leaning over
            if (p.tile.sameAs(item.tile)) {
                handleAction(p, item, opt)
            } else {
                p.writeMessage(Entity.YOU_CANT_REACH_THAT)
            }
            break
        }
    }

    private fun handleAction(p: Player, groundItem: GroundItem, opt: Int) {
        if (!p.world.isSpawned(groundItem)) {
            return
        }
        if (opt == 3) {
            if (!p.world.plugins.canPickupGroundItem(p, groundItem.item)) {
                return
            }
            // NOTE(Tom): we may want to make the assureFullInsertion flag false and
            // allow the world to remove some of the ground item instead of all of it.
            val add = p.inventory.add(item = groundItem.item, amount = groundItem.amount, assureFullInsertion = true)
            if (add.completed == 0) {
                p.writeMessage("You don't have enough inventory space to hold that item.")
                return
            }

            add.items.firstOrNull()?.let { item ->
                item.item.attr.putAll(groundItem.attr)
            }

            p.world.remove(groundItem)

            p.attr[GROUNDITEM_PICKUP_TRANSACTION] = WeakReference(add)
            p.world.plugins.executeGlobalGroundItemPickUp(p)
            p.world.getService(LoggerService::class.java, searchSubclasses = true)?.logItemPickUp(p, Item(groundItem.item, add.completed))
        } else if (opt == ITEM_ON_GROUND_ITEM_OPTION) {
            val item = p.attr[INTERACTING_ITEM]?.get() ?: return
            val handled = p.world.plugins.executeItemOnGroundItem(p, item.id, groundItem.item)

            if (!handled && p.world.devContext.debugItemActions) {
                p.writeMessage("Unhandled item on ground item action: [item=${item.id}, ground=${groundItem.item}]")
            }
        } else {
            val handled = p.world.plugins.executeGroundItem(p, groundItem.item, opt)
            if (!handled && p.world.devContext.debugItemActions) {
                val definition = p.world.definitions.get(ItemDef::class.java, groundItem.item)
                p.writeMessage("Unhandled ground item action: [item=${groundItem.item}, option=[$opt, ${definition.groundMenu[opt - 1]}]]")
            }
        }
    }
}