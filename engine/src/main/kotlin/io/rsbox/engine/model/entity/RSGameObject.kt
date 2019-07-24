package io.rsbox.engine.model.entity

import com.google.common.base.MoreObjects
import io.rsbox.engine.fs.DefinitionSet
import io.rsbox.engine.fs.def.ObjectDef
import io.rsbox.engine.fs.def.VarbitDef
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.AttributeMap
import io.rsbox.api.entity.GameObject
import io.rsbox.engine.model.timer.TimerMap

/**
 * A [RSGameObject] is any type of map object that can occupy a tile.
 *
 * @author Tom <rspsmods@gmail.com>
 */
abstract class RSGameObject : RSEntity, GameObject {

    /**
     * The object id.
     */
    val id: Int

    /**
     * A bit-packed byte that holds the object "type" and "rotation".
     */
    val settings: Byte

    /**
     * @see [AttributeMap]
     */
    val attr = AttributeMap()

    /**
     * @see [TimerMap]
     */
    val timers = TimerMap()

    val type: Int get() = settings.toInt() shr 2

    val rot: Int get() = settings.toInt() and 3

    private constructor(id: Int, settings: Int, tile: RSTile) {
        this.id = id
        this.settings = settings.toByte()
        this.tile = tile
    }

    constructor(id: Int, type: Int, rot: Int, tile: RSTile) : this(id, (type shl 2) or rot, tile)

    fun getDef(definitions: DefinitionSet): ObjectDef = definitions.get(ObjectDef::class.java, id)

    fun isSpawned(world: RSWorld): Boolean = world.isSpawned(this)

    /**
     * This method will get the "visually correct" object id for this npc from
     * [player]'s view point.
     *
     * Objects can change their appearance for each player depending on their
     * [ObjectDef.transforms] and [ObjectDef.varp]/[ObjectDef.varbit].
     */
    fun getTransform(player: RSPlayer): Int {
        val world = player.world
        val def = getDef(world.definitions)

        if (def.varbit != -1) {
            val varbitDef = world.definitions.get(VarbitDef::class.java, def.varbit)
            val state = player.varps.getBit(varbitDef.varp, varbitDef.startBit, varbitDef.endBit)
            return def.transforms!![state]
        }

        if (def.varp != -1) {
            val state = player.varps.getState(def.varp)
            return def.transforms!![state]
        }

        return id
    }

    override fun toString(): String = MoreObjects.toStringHelper(this).add("id", id).add("type", type).add("rot", rot).add("tile", tile.toString()).toString()
}