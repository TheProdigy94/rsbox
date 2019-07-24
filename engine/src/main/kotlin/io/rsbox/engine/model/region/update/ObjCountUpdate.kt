package io.rsbox.engine.model.region.update

import io.rsbox.engine.message.Message
import io.rsbox.engine.message.impl.ObjCountMessage
import io.rsbox.engine.model.entity.RSGroundItem

/**
 * Represents an update where a [RSGroundItem]'s [RSGroundItem.amount] is changed.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class ObjCountUpdate(override val type: EntityUpdateType, override val entity: RSGroundItem,
                     private val oldAmount: Int, private val newAmount: Int) : EntityUpdate<RSGroundItem>(type, entity) {

    override fun toMessage(): Message = ObjCountMessage(entity.item, oldAmount, newAmount, ((entity.tile.x and 0x7) shl 4) or (entity.tile.z and 0x7))
}