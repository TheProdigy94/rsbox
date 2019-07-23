package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
class IfOpenSubMessage(val parent: Int, val child: Int, val component: Int, val type: Int) : Message