package io.rsbox.engine.model.combat

import io.rsbox.engine.model.Hit

/**
 * Represents a [Hit] dealt by a [io.rsbox.engine.model.entity.RSPawn].
 *
 * @param landed
 * If the hit past the accuracy formula check (hit should land a random number
 * based on max hit)
 *
 * @author Tom <rspsmods@gmail.com>
 */
data class PawnHit(val hit: Hit, val landed: Boolean)