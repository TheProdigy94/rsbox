package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
data class ResumePauseButtonMessage(val interfaceId: Int, val component: Int, val slot: Int) : Message