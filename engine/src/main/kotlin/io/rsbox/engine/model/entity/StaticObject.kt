package io.rsbox.engine.model.entity

import io.rsbox.engine.model.EntityType
import io.rsbox.engine.model.RSTile

/**
 * A [StaticObject] is a game object that is part of the static terrain loaded
 * from the game's resources.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class StaticObject(id: Int, type: Int, rot: Int, tile: RSTile) : RSGameObject(id, type, rot, tile) {

    override val entityType: EntityType = EntityType.STATIC_OBJECT
}