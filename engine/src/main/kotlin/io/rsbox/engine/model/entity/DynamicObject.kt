package io.rsbox.engine.model.entity

import io.rsbox.engine.model.EntityType
import io.rsbox.engine.model.Tile

/**
 * A [DynamicObject] is a game object that can be spawned by the [io.rsbox.engine.model.RSWorld].
 *
 * @author Tom <rspsmods@gmail.com>
 */
class DynamicObject(id: Int, type: Int, rot: Int, tile: Tile) : GameObject(id, type, rot, tile) {

    constructor(other: GameObject) : this(other.id, other.type, other.rot, Tile(other.tile))

    constructor(other: GameObject, id: Int) : this(id, other.type, other.rot, Tile(other.tile))

    override val entityType: EntityType = EntityType.DYNAMIC_OBJECT
}