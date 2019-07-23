package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message
import io.rsbox.engine.message.MessageEncoderSet
import io.rsbox.engine.message.MessageStructureSet
import io.rsbox.engine.model.region.update.EntityGroupMessage

/**
 * @author Tom <rspsmods@gmail.com>
 */
class UpdateZonePartialEnclosedMessage(val x: Int, val z: Int, val encoders: MessageEncoderSet, val structures: MessageStructureSet,
                                       vararg val messages: EntityGroupMessage) : Message