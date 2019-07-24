package io.rsbox.engine.action

import io.rsbox.api.*
import io.rsbox.engine.message.impl.SetMapFlagMessage
import io.rsbox.engine.model.MovementQueue
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.entity.RSEntity
import io.rsbox.engine.model.entity.RSNpc
import io.rsbox.engine.model.entity.RSPawn
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.model.path.PathRequest
import io.rsbox.api.TaskPriority
import io.rsbox.api.entity.Npc
import io.rsbox.engine.model.item.RSItem
import io.rsbox.engine.model.queue.QueueTask
import io.rsbox.engine.model.timer.FROZEN_TIMER
import io.rsbox.engine.model.timer.RESET_PAWN_FACING_TIMER
import io.rsbox.engine.model.timer.STUN_TIMER
import io.rsbox.engine.oldplugin.Plugin
import io.rsbox.util.AabbUtil
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
object PawnPathAction {

    private const val ITEM_USE_OPCODE = -1

    val walkPlugin: Plugin.() -> Unit = {
        val pawn = ctx as RSPawn
        val world = pawn.world
        val other = pawn.attr[INTERACTING_NPC_ATTR]?.get() ?: pawn.attr[INTERACTING_PLAYER_ATTR]?.get()!!
        val opt = pawn.attr[INTERACTING_OPT_ATTR]!!

        /*
         * Some interactions only require line-of-sight range, such as npcs
         * behind cells or booths. This allows for diagonal interaction.
         *
         * Set to null for default interaction range.
         */
        val lineOfSightRange = if (other is RSNpc) world.plugins.getNpcInteractionDistance(other.id) else null

        pawn.queue(TaskPriority.STANDARD) {
            terminateAction = {
                pawn.stopMovement()
                if (pawn is RSPlayer) {
                    pawn.write(SetMapFlagMessage(255, 255))
                }
            }

            walk(this, pawn, other as RSPawn, opt, lineOfSightRange)
        }
    }

    val itemUsePlugin: Plugin.() -> Unit = s@ {
        val pawn = ctx as RSPawn
        val world = pawn.world
        val other = pawn.attr[INTERACTING_NPC_ATTR]?.get() ?: pawn.attr[INTERACTING_PLAYER_ATTR]?.get()!!

        /*
         * Some interactions only require line-of-sight range, such as npcs
         * behind cells or booths. This allows for diagonal interaction.
         *
         * Set to null for default interaction range.
         */
        val lineOfSightRange = if (other is RSNpc) world.plugins.getNpcInteractionDistance(other.id) else null

        pawn.queue(TaskPriority.STANDARD) {
            terminateAction = {
                pawn.stopMovement()
                if (pawn is RSPlayer) {
                    pawn.write(SetMapFlagMessage(255, 255))
                }
            }

            walk(this, pawn, other as RSPawn, ITEM_USE_OPCODE, lineOfSightRange)
        }
    }

    private suspend fun walk(it: QueueTask, pawn: RSPawn, other: RSPawn, opt: Int, lineOfSightRange: Int?) {
        val world = pawn.world
        val initialTile = RSTile(other.tile as RSTile)

        pawn.facePawn(other)

        val pathFound = walkTo(it, pawn, other, interactionRange = lineOfSightRange ?: 1, lineOfSight = lineOfSightRange != null)
        if (!pathFound) {
            pawn.movementQueue.clear()
            if (pawn is RSPlayer) {
                when {
                    pawn.timers.has(FROZEN_TIMER) -> pawn.writeMessage(RSEntity.MAGIC_STOPS_YOU_FROM_MOVING)
                    pawn.timers.has(STUN_TIMER) -> pawn.writeMessage(RSEntity.YOURE_STUNNED)
                    else -> pawn.writeMessage(RSEntity.YOU_CANT_REACH_THAT)
                }
                pawn.write(SetMapFlagMessage(255, 255))
            }
            pawn.resetFacePawn()
            return
        }

        pawn.stopMovement()

        if (pawn is RSPlayer) {
            if (pawn.attr[FACING_PAWN_ATTR]?.get() != other) {
                return
            }
            /*
             * If the npc has moved from the time this queue was added to
             * when it was actually invoked, we need to walk towards it again.
             */
            if (!other.tile.sameAs(initialTile)) {
                walk(it, pawn, other, opt, lineOfSightRange)
                return
            }

            if (other is RSNpc) {

                /*
                 * On 07, only one npc can be facing the player at a time,
                 * so if the last pawn that faced the player is still facing
                 * them, then we reset their face target.
                 */
                pawn.attr[NPC_FACING_US_ATTR]?.get()?.let {
                    if ((it as RSNpc).attr[FACING_PAWN_ATTR]?.get() == pawn) {
                        it.resetFacePawn()
                        it.timers.remove(RESET_PAWN_FACING_TIMER)
                    }
                }
                pawn.attr[NPC_FACING_US_ATTR] = WeakReference(other as Npc)

                /*
                 * Stop the npc from walking while the player talks to it
                 * for [RSNpc.RESET_PAWN_FACE_DELAY] cycles.
                 */
                other.stopMovement()
                if (other.attr[FACING_PAWN_ATTR]?.get() != pawn) {
                    other.facePawn(pawn)
                    other.timers[RESET_PAWN_FACING_TIMER] = RSNpc.RESET_PAWN_FACE_DELAY
                }

                val npcId = other.getTransform(pawn)
                val handled = if (opt != ITEM_USE_OPCODE) {
                    world.plugins.executeNpc(pawn, npcId, opt)
                } else {
                    val item = pawn.attr[INTERACTING_ITEM]?.get() as RSItem? ?: return
                    world.plugins.executeItemOnNpc(pawn, npcId, item.id)
                }

                if (!handled) {
                    pawn.writeMessage(RSEntity.NOTHING_INTERESTING_HAPPENS)
                }
            }

            if (other is RSPlayer) {
                val option = pawn.options[opt - 1]
                if (option != null) {
                    val handled = world.plugins.executePlayerOption(pawn, option)
                    if (!handled) {
                        pawn.writeMessage(RSEntity.NOTHING_INTERESTING_HAPPENS)
                    }
                }
            }
            pawn.resetFacePawn()
            pawn.faceTile(other.tile as RSTile)
        }
    }

    suspend fun walkTo(it: QueueTask, pawn: RSPawn, target: RSPawn, interactionRange: Int, lineOfSight: Boolean): Boolean {
        val sourceSize = pawn.getSize()
        val targetSize = target.getSize()
        val sourceTile = pawn.tile
        val targetTile = target.tile
        val projectile = interactionRange > 2

        val frozen = pawn.timers.has(FROZEN_TIMER)
        val stunned = pawn.timers.has(STUN_TIMER)

        if (pawn.attr[FACING_PAWN_ATTR]?.get() != target) {
            return false
        }

        if (stunned) {
            return false
        }

        if (frozen) {
            if (overlap(sourceTile as RSTile, sourceSize, targetTile as RSTile, targetSize)) {
                return false
            }

            if (!projectile) {
                return if (!lineOfSight) {
                    bordering(sourceTile, sourceSize, targetTile, interactionRange)
                } else {
                    overlap(sourceTile, sourceSize, targetTile, interactionRange) && (interactionRange == 0 || !sourceTile.sameAs(targetTile))
                            && pawn.world.collision.raycast(sourceTile, targetTile, lineOfSight)
                }
            }
        }

        val builder = PathRequest.Builder()
                .setPoints(sourceTile as RSTile, targetTile as RSTile)
                .setSourceSize(sourceSize, sourceSize)
                .setTargetSize(targetSize, targetSize)
                .setProjectilePath(lineOfSight || projectile)
                .setTouchRadius(interactionRange)
                .clipPathNodes(node = true, link = true)

        if (!lineOfSight && !projectile) {
            builder.clipOverlapTiles().clipDiagonalTiles()
        }

        val route = pawn.createPathFindingStrategy().calculateRoute(builder.build())
        pawn.walkPath(route.path, MovementQueue.StepType.NORMAL, detectCollision = true)

        while (!pawn.tile.sameAs(route.tail)) {
            if (!targetTile.sameAs(target.tile)) {
                return walkTo(it, pawn, target, interactionRange, lineOfSight)
            }
            it.wait(1)
        }

        return route.success
    }

    private fun overlap(tile1: RSTile, size1: Int, tile2: RSTile, size2: Int): Boolean = AabbUtil.areOverlapping(tile1.x, tile1.z, size1, size1, tile2.x, tile2.z, size2, size2)

    private fun bordering(tile1: RSTile, size1: Int, tile2: RSTile, size2: Int): Boolean = AabbUtil.areBordering(tile1.x, tile1.z, size1, size1, tile2.x, tile2.z, size2, size2)
}