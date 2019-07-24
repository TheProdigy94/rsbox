package io.rsbox.engine.model.region.update

import io.rsbox.engine.message.Message
import io.rsbox.engine.message.impl.ObjAddMessage
import io.rsbox.engine.model.entity.RSGroundItem

/**
 * Represents an update where a [RSGroundItem] is spawned.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class ObjAddUpdate(override val type: EntityUpdateType,
                   override val entity: RSGroundItem) : EntityUpdate<RSGroundItem>(type, entity) {

    override fun toMessage(): Message = ObjAddMessage(entity.item, entity.amount, ((entity.tile.x and 0x7) shl 4) or (entity.tile.z and 0x7))
}