package io.rsbox.engine.model.path

import io.rsbox.engine.model.RSTile
import java.util.*

/**
 * Represents a path from Point A to Point B.
 *
 * @param path
 * A [Queue] containing the path from Point A to Point B, not including the
 * start tile from Point A.
 *
 * @param success
 * If the path was able to successfully find a path from Point A to Point B,
 * completely. In other words, [Queue.last] for [path] should be equal to
 * Point B.
 *
 * @param tail
 * The last tile in our [path].
 *
 * @author Tom <rspsmods@gmail.com>
 */
data class Route(val path: Queue<RSTile>, val success: Boolean, val tail: RSTile)