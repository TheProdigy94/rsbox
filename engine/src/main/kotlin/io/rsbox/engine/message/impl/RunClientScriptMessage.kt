package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

/**
 * @author Tom <rspsmods@gmail.com>
 */
class RunClientScriptMessage(val id: Int, vararg val args: Any) : Message