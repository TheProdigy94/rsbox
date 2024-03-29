package io.rsbox.engine.action

import io.rsbox.engine.fs.def.ObjectDef
import io.rsbox.api.Direction
import io.rsbox.engine.model.MovementQueue
import io.rsbox.api.INTERACTING_ITEM
import io.rsbox.api.INTERACTING_OBJ_ATTR
import io.rsbox.api.INTERACTING_OPT_ATTR
import io.rsbox.engine.model.collision.ObjectType
import io.rsbox.engine.model.entity.RSEntity
import io.rsbox.engine.model.entity.RSGameObject
import io.rsbox.engine.model.entity.RSPawn
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.model.path.PathRequest
import io.rsbox.engine.model.path.Route
import io.rsbox.api.TaskPriority
import io.rsbox.engine.message.impl.SetMapFlagMessage
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.item.RSItem
import io.rsbox.engine.model.queue.RSQueueTask
import io.rsbox.engine.model.timer.FROZEN_TIMER
import io.rsbox.engine.model.timer.STUN_TIMER
import io.rsbox.engine.oldplugin.Plugin
import io.rsbox.util.AabbUtil
import io.rsbox.util.DataConstants
import java.util.ArrayDeque
import java.util.EnumSet

/**
 * This class is responsible for calculating distances and valid interaction
 * tiles for [RSGameObject] path-finding.
 *
 * @author Tom <rspsmods@gmail.com>
 */
object ObjectPathAction {

    fun walk(player: RSPlayer, obj: RSGameObject, lineOfSightRange: Int?, logic: Plugin.() -> Unit) {
        player.queue(TaskPriority.STANDARD) { this as RSQueueTask
            terminateAction = {
                player.stopMovement()
                player.write(SetMapFlagMessage(255, 255))
            }

            val route = walkTo(obj, lineOfSightRange)
            if (route.success) {
                if (lineOfSightRange == null || lineOfSightRange > 0) {
                    faceObj(player, obj)
                }
                player.executePlugin(logic)
            } else {
                player.faceTile(obj.tile as RSTile)
                when {
                    player.timers.has(FROZEN_TIMER) -> player.writeMessage(RSEntity.MAGIC_STOPS_YOU_FROM_MOVING)
                    player.timers.has(STUN_TIMER) -> player.writeMessage(RSEntity.YOURE_STUNNED)
                    else -> player.writeMessage(RSEntity.YOU_CANT_REACH_THAT)
                }
                player.write(SetMapFlagMessage(255, 255))
            }
        }
    }

    val itemOnObjectPlugin: Plugin.() -> Unit = {
        val player = ctx as RSPlayer

        val item = player.attr[INTERACTING_ITEM]!!.get()!! as RSItem
        val obj = player.attr[INTERACTING_OBJ_ATTR]!!.get()!! as RSGameObject
        val lineOfSightRange = player.world.plugins.getObjInteractionDistance(obj.id)

        walk(player, obj, lineOfSightRange) {
            if (!player.world.plugins.executeItemOnObject(player, obj.getTransform(player), item.id)) {
                player.writeMessage(RSEntity.NOTHING_INTERESTING_HAPPENS)
                if (player.world.devContext.debugObjects) {
                    player.writeMessage("Unhandled item on object: [item=$item, id=${obj.id}, type=${obj.type}, rot=${obj.rot}, x=${obj.tile.x}, z=${obj.tile.z}]")
                }
            }
        }
    }

    val objectInteractPlugin: Plugin.() -> Unit = {
        val player = ctx as RSPlayer

        val obj = player.attr[INTERACTING_OBJ_ATTR]!!.get()!! as RSGameObject
        val opt = player.attr[INTERACTING_OPT_ATTR]
        val lineOfSightRange = player.world.plugins.getObjInteractionDistance(obj.id)

        walk(player, obj, lineOfSightRange) {
            if (!player.world.plugins.executeObject(player, obj.getTransform(player), opt!!)) {
                player.writeMessage(RSEntity.NOTHING_INTERESTING_HAPPENS)
                if (player.world.devContext.debugObjects) {
                    player.writeMessage("Unhandled object action: [opt=$opt, id=${obj.id}, type=${obj.type}, rot=${obj.rot}, x=${obj.tile.x}, z=${obj.tile.z}]")
                }
            }
        }
    }

    private suspend fun RSQueueTask.walkTo(obj: RSGameObject, lineOfSightRange: Int?): Route {
        val pawn = ctx as RSPawn

        val def = obj.getDef(pawn.world.definitions)
        val tile = obj.tile
        val type = obj.type
        val rot = obj.rot
        var width = def.width
        var length = def.length
        val clipMask = def.clipMask

        val wall = type == ObjectType.LENGTHWISE_WALL.value || type == ObjectType.DIAGONAL_WALL.value
        val diagonal = type == ObjectType.DIAGONAL_WALL.value || type == ObjectType.DIAGONAL_INTERACTABLE.value
        val wallDeco = type == ObjectType.INTERACTABLE_WALL_DECORATION.value || type == ObjectType.INTERACTABLE_WALL.value
        val blockDirections = EnumSet.noneOf(Direction::class.java)

        if (wallDeco) {
            width = 0
            length = 0
        } else if (!wall && (rot == 1 || rot == 3)) {
            width = def.length
            length = def.width
        }

        /*
         * Objects have a clip mask in their [ObjectDef] which can be used
         * to specify any directions that the object can't be 'interacted'
         * from.
         */
        val blockBits = 4
        val clipFlag = (DataConstants.BIT_MASK[blockBits] and (clipMask shl rot)) or (clipMask shr (blockBits - rot))

        if ((0x1 and clipFlag) != 0) {
            blockDirections.add(Direction.NORTH)
        }

        if ((0x2 and clipFlag) != 0) {
            blockDirections.add(Direction.EAST)
        }

        if ((0x4 and clipFlag) != 0) {
            blockDirections.add(Direction.SOUTH)
        }

        if ((clipFlag and 0x8) != 0) {
            blockDirections.add(Direction.WEST)
        }

        /*
         * Wall objects can't be interacted from certain directions due to
         * how they are visually placed in a tile.
         */
        val blockedWallDirections = when (rot) {
            0 -> EnumSet.of(Direction.EAST)
            1 -> EnumSet.of(Direction.SOUTH)
            2 -> EnumSet.of(Direction.WEST)
            3 -> EnumSet.of(Direction.NORTH)
            else -> throw IllegalStateException("Invalid object rotation: $rot")
        }

        /*
         * Diagonal walls have an extra direction set as 'blocked', this is to
         * avoid the player interacting with the door and having its opened
         * door object be spawned on top of them, which leads to them being
         * stuck.
         */
        if (wall && diagonal) {
            when (rot) {
                0 -> blockedWallDirections.add(Direction.NORTH)
                1 -> blockedWallDirections.add(Direction.EAST)
                2 -> blockedWallDirections.add(Direction.SOUTH)
                3 -> blockedWallDirections.add(Direction.WEST)
            }
        }

        if (wall) {
            /*
             * Check if the pawn is within interaction distance of the wall.
             */
            if (pawn.tile.isWithinRadius(tile, 1)) {
                val dir = Direction.between(tile as RSTile, pawn.tile as RSTile)
                if (dir !in blockedWallDirections && (diagonal || !AabbUtil.areDiagonal(pawn.tile.x, pawn.tile.z, pawn.getSize(), pawn.getSize(), tile.x, tile.z, width, length))) {
                    return Route(ArrayDeque(), success = true, tail = pawn.tile as RSTile)
                }
            }

            blockDirections.addAll(blockedWallDirections)
        }

        val builder = PathRequest.Builder()
                .setPoints(pawn.tile as RSTile, tile as RSTile)
                .setSourceSize(pawn.getSize(), pawn.getSize())
                .setProjectilePath(lineOfSightRange != null)
                .setTargetSize(width, length)
                .clipPathNodes(node = true, link = true)
                .clipDirections(*blockDirections.toTypedArray())

        if (lineOfSightRange != null) {
            builder.setTouchRadius(lineOfSightRange)
        }

        /*
         * If the object is not a 'diagonal' object, you shouldn't be able to
         * interact with them from diagonal tiles.
         */
        if (!diagonal) {
            builder.clipDiagonalTiles()
        }

        /*
         * If the object is not a wall object, or if we have a line of sight range
         * set for the object, then we shouldn't clip the tiles that overlap the
         * object; otherwise we do clip them.
         */
        if (!wall && (lineOfSightRange == null || lineOfSightRange > 0)) {
            builder.clipOverlapTiles()
        }

        val route = pawn.createPathFindingStrategy().calculateRoute(builder.build())

        if (pawn.timers.has(FROZEN_TIMER) && !pawn.tile.sameAs(route.tail)) {
            return Route(ArrayDeque(), success = false, tail = pawn.tile as RSTile)
        }

        pawn.walkPath(route.path, MovementQueue.StepType.NORMAL, detectCollision = true)

        val last = pawn.movementQueue.peekLast()
        while (last != null && !pawn.tile.sameAs(last) && !pawn.timers.has(FROZEN_TIMER) && !pawn.timers.has(STUN_TIMER) && pawn.lock.canMove()) {
            wait(1)
        }

        if (pawn.timers.has(STUN_TIMER)) {
            pawn.stopMovement()
            return Route(ArrayDeque(), success = false, tail = pawn.tile as RSTile)
        }

        if (pawn.timers.has(FROZEN_TIMER) && !pawn.tile.sameAs(route.tail)) {
            return Route(ArrayDeque(), success = false, tail = pawn.tile as RSTile)
        }

        if (wall && !route.success && pawn.tile.isWithinRadius(tile, 1) && Direction.between(tile, pawn.tile as RSTile) !in blockedWallDirections) {
            return Route(route.path, success = true, tail = route.tail)
        }

        return route
    }

    private fun faceObj(pawn: RSPawn, obj: RSGameObject) {
        val def = pawn.world.definitions.get(ObjectDef::class.java, obj.id)
        val rot = obj.rot
        val type = obj.type

        when (type) {
            ObjectType.LENGTHWISE_WALL.value -> {
                if (!pawn.tile.sameAs(obj.tile)) {
                    pawn.faceTile(obj.tile as RSTile)
                }
            }
            ObjectType.INTERACTABLE_WALL_DECORATION.value, ObjectType.INTERACTABLE_WALL.value -> {
                val dir = when (rot) {
                    0 -> Direction.WEST
                    1 -> Direction.NORTH
                    2 -> Direction.EAST
                    3 -> Direction.SOUTH
                    else -> throw IllegalStateException("Invalid object rotation: $obj")
                }
                pawn.faceTile(pawn.tile.step(dir) as RSTile)
            }
            else -> {
                var width = def.width
                var length = def.length
                if (rot == 1 || rot == 3) {
                    width = def.length
                    length = def.width
                }
                pawn.faceTile(obj.tile.transform(width shr 1, length shr 1) as RSTile, width, length)
            }
        }
    }
}