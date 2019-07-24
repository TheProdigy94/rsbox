package io.rsbox.engine.model.entity

import io.rsbox.api.Tile
import io.rsbox.api.entity.Entity
import io.rsbox.engine.model.EntityType
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld

/**
 * An [RSEntity] can be anything in the world that that maintains a [RSTile].
 *
 * @author Tom <rspsmods@gmail.com>
 */
abstract class RSEntity : Entity {

    /**
     * The current 3D [RSTile] that this [RSPawn] is standing on in the [RSWorld].
     */
    override lateinit var tile: Tile

    abstract val entityType: EntityType

    companion object {
        const val NOTHING_INTERESTING_HAPPENS = "Nothing interesting happens."
        const val YOU_CANT_REACH_THAT = "I can't reach that!"
        const val MAGIC_STOPS_YOU_FROM_MOVING = "A magical force stops you from moving."
        const val YOURE_STUNNED = "You're stunned!"
    }
}