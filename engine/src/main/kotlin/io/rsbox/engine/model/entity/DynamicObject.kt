package io.rsbox.engine.model.entity

import io.rsbox.engine.model.EntityType
import io.rsbox.engine.model.RSTile

/**
 * A [DynamicObject] is a game object that can be spawned by the [io.rsbox.engine.model.RSWorld].
 *
 * @author Tom <rspsmods@gmail.com>
 */
class DynamicObject(id: Int, type: Int, rot: Int, tile: RSTile) : RSGameObject(id, type, rot, tile) {

    constructor(other: RSGameObject) : this(other.id, other.type, other.rot, RSTile(other.tile as RSTile))

    constructor(other: RSGameObject, id: Int) : this(id, other.type, other.rot, RSTile(other.tile as RSTile))

    override val entityType: EntityType = EntityType.DYNAMIC_OBJECT
}