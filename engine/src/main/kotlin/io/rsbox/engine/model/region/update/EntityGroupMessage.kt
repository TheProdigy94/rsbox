package io.rsbox.engine.model.region.update

import io.rsbox.engine.message.Message

/**
 * Represents a group of [EntityUpdate]s.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class EntityGroupMessage(val id: Int, val message: Message)