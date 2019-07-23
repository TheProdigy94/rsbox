package io.rsbox.engine.model.region.update

import io.rsbox.engine.message.Message
import io.rsbox.engine.message.impl.SoundAreaMessage
import io.rsbox.engine.model.entity.AreaSound

/**
 * Represents an update where a [AreaSound] is spawned.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class SoundAreaUpdate(override val type: EntityUpdateType,
                      override val entity: AreaSound) : EntityUpdate<AreaSound>(type, entity) {

    override fun toMessage(): Message = SoundAreaMessage(((entity.tile.x and 0x7) shl 4) or (entity.tile.z and 0x7), entity.id,
            entity.radius, entity.volume, entity.delay)
}