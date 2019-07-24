package io.rsbox.engine.model.region.update

import com.google.common.base.MoreObjects
import io.rsbox.engine.message.Message
import io.rsbox.engine.model.entity.RSEntity

/**
 * Represents an update for an [RSEntity], in which they can be spawned
 * or removed from a client's viewport.
 *
 * @author Tom <rspsmods@gmail.com>
 */
abstract class EntityUpdate<T : RSEntity>(open val type: EntityUpdateType, open val entity: T) {

    abstract fun toMessage(): Message

    override fun toString(): String = MoreObjects.toStringHelper(this).add("type", type).add("entity", entity).toString()
}