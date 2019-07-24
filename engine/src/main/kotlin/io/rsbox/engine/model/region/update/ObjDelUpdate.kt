package io.rsbox.engine.model.region.update

import io.rsbox.engine.message.Message
import io.rsbox.engine.message.impl.ObjDelMessage
import io.rsbox.engine.model.entity.RSGroundItem

/**
 * Represents an update where a [RSGroundItem] is removed.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class ObjDelUpdate(override val type: EntityUpdateType,
                   override val entity: RSGroundItem) : EntityUpdate<RSGroundItem>(type, entity) {

    override fun toMessage(): Message = ObjDelMessage(entity.item, ((entity.tile.x and 0x7) shl 4) or (entity.tile.z and 0x7))
}