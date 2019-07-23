package io.rsbox.engine.plugin

import io.rsbox.engine.model.entity.Player

/**
 * Represents a plugin that can be executed at any time by a context.
 *
 * @param ctx
 * Can be anything from [Player] to [io.rsbox.engine.model.entity.Pawn].
 *
 * @author Tom <rspsmods@gmail.com>
 */
class Plugin(var ctx: Any?)