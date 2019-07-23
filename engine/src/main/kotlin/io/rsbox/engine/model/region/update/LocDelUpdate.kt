package io.rsbox.engine.model.region.update

import io.rsbox.engine.message.Message
import io.rsbox.engine.message.impl.LocDelMessage
import io.rsbox.engine.model.entity.GameObject

/**
 * Represents an update where a [GameObject] is removed.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class LocDelUpdate(override val type: EntityUpdateType,
                   override val entity: GameObject) : EntityUpdate<GameObject>(type, entity) {

    override fun toMessage(): Message = LocDelMessage(entity.settings.toInt(),
            ((entity.tile.x and 0x7) shl 4) or (entity.tile.z and 0x7))
}