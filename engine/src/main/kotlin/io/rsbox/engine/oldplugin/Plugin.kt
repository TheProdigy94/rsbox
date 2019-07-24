package io.rsbox.engine.oldplugin

import io.rsbox.engine.model.entity.RSPlayer

/**
 * Represents a oldplugin that can be executed at any time by a context.
 *
 * @param ctx
 * Can be anything from [RSPlayer] to [io.rsbox.engine.model.entity.RSPawn].
 *
 * @author Tom <rspsmods@gmail.com>
 */
class Plugin(var ctx: Any?)