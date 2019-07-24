package io.rsbox.engine.model.combat

import io.rsbox.engine.model.EntityType
import io.rsbox.engine.model.entity.RSPawn
import java.util.*

/**
 * Represents a map of hits from different [RSPawn]s and their information.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class DamageMap {

    private val map = WeakHashMap<RSPawn, DamageStack>(0)

    operator fun get(pawn: RSPawn): DamageStack? = map[pawn]

    fun add(pawn: RSPawn, damage: Int) {
        val total = (map[pawn]?.totalDamage ?: 0) + damage
        map[pawn] = DamageStack(total, System.currentTimeMillis())
    }

    /**
     * Get all [DamageStack]s dealt by [RSPawn]s whom meets the criteria
     * [RSPawn.entityType] == [type].
     */
    fun getAll(type: EntityType, timeFrameMs: Long? = null): Collection<DamageStack> = map.filter { it.key.entityType == type && (timeFrameMs == null || System.currentTimeMillis() - it.value.lastHit < timeFrameMs) }.values

    /**
     * Get the total damage from a [pawn].
     *
     * @return
     * 0 if [pawn] has not dealt any damage.
     */
    fun getDamageFrom(pawn: RSPawn): Int = map[pawn]?.totalDamage ?: 0

    /**
     * Gets the [RSPawn] that has dealt the most damage in this map.
     */
    fun getMostDamage(): RSPawn? = map.maxBy { it.value.totalDamage }?.key

    /**
     * Gets the most damage dealt by a [RSPawn] in our map whom meets the criteria
     * [RSPawn.entityType] == [type].
     */
    fun getMostDamage(type: EntityType, timeFrameMs: Long? = null): RSPawn? = map.filter { it.key.entityType == type && (timeFrameMs == null || System.currentTimeMillis() - it.value.lastHit < timeFrameMs) }.maxBy { it.value.totalDamage }?.key

    data class DamageStack(val totalDamage: Int, val lastHit: Long)
}