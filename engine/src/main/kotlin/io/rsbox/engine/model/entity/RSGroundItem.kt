package io.rsbox.engine.model.entity

import com.google.common.base.MoreObjects
import io.rsbox.api.entity.GroundItem
import io.rsbox.engine.model.EntityType
import io.rsbox.engine.model.PlayerUID
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.item.RSItem
import io.rsbox.engine.model.item.ItemAttribute
import java.util.EnumMap

/**
 * An item that is spawned on the ground.
 *
 * @param ownerUID
 * If null, the item will be visible and can be interacted with by any player
 * in the world. Otherwise, it will only be visible to the player who's [RSPlayer.uid]
 * matches [ownerUID].
 *
 * @author Tom <rspsmods@gmail.com>
 */
class RSGroundItem private constructor(val item: Int, var amount: Int, internal var ownerUID: PlayerUID?) : RSEntity(), GroundItem {

    constructor(item: Int, amount: Int, tile: RSTile, owner: RSPlayer? = null) : this(item, amount, owner?.uid) {
        this.tile = tile
    }

    constructor(item: RSItem, tile: RSTile, owner: RSPlayer? = null) : this(item.id, item.amount, tile, owner)

    internal var currentCycle = 0

    internal var respawnCycles = -1

    internal val attr = EnumMap<ItemAttribute, Int>(ItemAttribute::class.java)

    override val entityType: EntityType = EntityType.GROUND_ITEM

    fun isOwnedBy(p: RSPlayer): Boolean = ownerUID != null && p.uid.value == ownerUID!!.value

    fun isPublic(): Boolean = ownerUID == null

    fun canBeViewedBy(p: RSPlayer): Boolean = isPublic() || isOwnedBy(p)

    fun removeOwner() {
        ownerUID = null
    }

    fun copyAttr(attributes: Map<ItemAttribute, Int>): RSGroundItem {
        attr.putAll(attributes)
        return this
    }

    fun isSpawned(world: RSWorld): Boolean = world.isSpawned(this)

    override fun toString(): String = MoreObjects.toStringHelper(this).add("item", item).add("amount", amount).add("tile", tile.toString()).add("owner", ownerUID).toString()

    companion object {
        /**
         * The default amount of cycles for this ground item to respawn if flagged
         * to do so.
         */
        const val DEFAULT_RESPAWN_CYCLES = 50

        /**
         * The default amount of cycles for this item to be publicly visible.
         */
        const val DEFAULT_PUBLIC_SPAWN_CYCLES = 100

        /**
         * The default amount of cycles for this item to despawn from the world.
         */
        const val DEFAULT_DESPAWN_CYCLES = 600
    }
}
