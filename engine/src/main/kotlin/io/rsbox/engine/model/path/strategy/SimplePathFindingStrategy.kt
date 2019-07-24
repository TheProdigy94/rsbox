package io.rsbox.engine.model.path.strategy

import io.rsbox.api.Direction
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.collision.CollisionManager
import io.rsbox.engine.model.path.PathFindingStrategy
import io.rsbox.engine.model.path.PathRequest
import io.rsbox.engine.model.path.Route
import io.rsbox.util.AabbUtil
import java.util.*

/**
 * @author Tom <rspsmods@gmail.com>
 */
class SimplePathFindingStrategy(collision: CollisionManager) : PathFindingStrategy(collision) {

    // TODO(Tom): redo this whole strategy (used for npcs). Fucking hate how
    // it is atm (jan 27 2019).

    override fun calculateRoute(request: PathRequest): Route {
        val start = request.start
        val end = request.end

        val projectile = request.projectilePath
        val sourceWidth = request.sourceWidth
        val sourceLength = request.sourceLength
        val targetWidth = Math.max(request.touchRadius, request.targetWidth)
        val targetLength = Math.max(request.touchRadius, request.targetLength)

        val path = ArrayDeque<RSTile>()
        var success = false

        var searchLimit = 2
        while (searchLimit-- > 0) {
            var tail = if (path.isNotEmpty()) path.peekLast() as RSTile else start
            if (areBordering(tail, sourceWidth, end, targetWidth) && !areDiagonal(tail, sourceWidth, end, targetWidth)
                    && collision.raycast(tail, end, projectile)) {
                success = true
                break
            }

            var eastOrWest = if (tail.x < end.x) Direction.EAST else Direction.WEST
            var northOrSouth = if (tail.z < end.z) Direction.NORTH else Direction.SOUTH
            var overlapped = false

            if (areOverlapping(tail, sourceWidth, end, targetWidth)) {
                eastOrWest = eastOrWest.getOpposite()
                northOrSouth = northOrSouth.getOpposite()
                overlapped = true
            }

            while ((!areCoordinatesInRange(tail.z, sourceLength, end.z, targetLength)
                            || areDiagonal(tail, sourceLength, end, targetLength)
                            || areOverlapping(tail, sourceLength, end, targetLength))
                    && (overlapped || !areOverlapping(tail.step(northOrSouth) as RSTile, sourceLength, end, targetLength))
                    && canTraverse(collision, tail, sourceWidth, sourceLength, northOrSouth, projectile)) {
                tail = tail.step(northOrSouth) as RSTile
                path.add(tail)
            }

            while ((!areCoordinatesInRange(tail.x, sourceWidth, end.x, targetWidth)
                            || areDiagonal(tail, sourceWidth, end, targetWidth)
                            || areOverlapping(tail, sourceWidth, end, targetWidth))
                    && (overlapped || !areOverlapping(tail.step(eastOrWest) as RSTile, sourceWidth, end, targetWidth))
                    && canTraverse(collision, tail, sourceWidth, sourceLength, eastOrWest, projectile)) {
                tail = tail.step(eastOrWest) as RSTile
                path.add(tail)
            }
        }

        return Route(path, success, tail = if (path.isNotEmpty()) path.peekLast() else start)
    }

    private fun canTraverse(collision: CollisionManager, tile: RSTile, width: Int, length: Int, direction: Direction, projectile: Boolean): Boolean {
        for (x in 0 until width) {
            for (z in 0 until length) {
                val transform = tile.transform(x, z)
                if (!collision.canTraverse(transform as RSTile, direction, projectile) || !collision.canTraverse(transform.step(direction) as RSTile, direction.getOpposite(), projectile)) {
                    return false
                }
            }
        }
        return true
    }

    private fun areBordering(tile1: RSTile, size1: Int, tile2: RSTile, size2: Int): Boolean = AabbUtil.areBordering(tile1.x, tile1.z, size1, size1, tile2.x, tile2.z, size2, size2)

    private fun areDiagonal(tile1: RSTile, size1: Int, tile2: RSTile, size2: Int): Boolean = AabbUtil.areDiagonal(tile1.x, tile1.z, size1, size1, tile2.x, tile2.z, size2, size2)

    private fun areOverlapping(tile1: RSTile, size1: Int, tile2: RSTile, size2: Int): Boolean = AabbUtil.areOverlapping(tile1.x, tile1.z, size1, size1, tile2.x, tile2.z, size2, size2)

    private fun areCoordinatesInRange(coord1: Int, size1: Int, coord2: Int, size2: Int): Boolean {
        val a = Pair(coord1, coord1 + size1)
        val b = Pair(coord2, coord2 + size2)

        if (a.second < b.first) {
            return false
        }

        if (a.first > b.second) {
            return false
        }

        return true
    }
}