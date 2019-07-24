package io.rsbox.engine.model.item

import com.google.common.base.MoreObjects
import io.rsbox.api.item.Item
import io.rsbox.engine.fs.DefinitionSet
import io.rsbox.engine.fs.def.ItemDef

/**
 * @author Tom <rspsmods@gmail.com>
 */
class RSItem(val id: Int, var amount: Int = 1) : Item {

    constructor(other: RSItem) : this(other.id, other.amount) {
        copyAttr(other)
    }

    constructor(other: RSItem, amount: Int) : this(other.id, amount) {
        copyAttr(other)
    }

    val attr = mutableMapOf<ItemAttribute, Int>()

    /**
     * Returns a <strong>new</strong> [RSItem] with the noted link as the item id.
     * If this item does not have a noted link item id, it will return a new [RSItem]
     * with the same [RSItem.id].
     */
    fun toNoted(definitions: DefinitionSet): RSItem {
        val def = getDef(definitions)
        return if (def.noteTemplateId == 0 && def.noteLinkId > 0) RSItem(def.noteLinkId, amount).copyAttr(this) else RSItem(this).copyAttr(this)
    }

    /**
     * Returns a <strong>new</strong> [RSItem] with the unnoted link as the item id.
     * If this item does not have a unnoted link item id, it will return a new [RSItem]
     * with the same [RSItem.id].
     */
    fun toUnnoted(definitions: DefinitionSet): RSItem {
        val def = getDef(definitions)
        return if (def.noteTemplateId > 0) RSItem(def.noteLinkId, amount).copyAttr(this) else RSItem(this).copyAttr(this)
    }

    /**
     * Get the name of this item. If this item is noted this method will use
     * its un-noted template and get the name for said template.
     */
    fun getName(definitions: DefinitionSet): String = toUnnoted(definitions).getDef(definitions).name

    fun getDef(definitions: DefinitionSet) = definitions.get(ItemDef::class.java, id)

    /**
     * Returns true if [attr] contains any value.
     */
    fun hasAnyAttr(): Boolean = attr.isNotEmpty()

    fun getAttr(attrib: ItemAttribute): Int? = attr[attrib]

    fun putAttr(attrib: ItemAttribute, value: Int): RSItem {
        attr[attrib] = value
        return this
    }

    /**
     * Copies the [RSItem.attr] map from [other] to this.
     */
    fun copyAttr(other: RSItem): RSItem {
        if (other.hasAnyAttr()) {
            attr.putAll(other.attr)
        }
        return this
    }

    override fun toString(): String = MoreObjects.toStringHelper(this).add("id", id).add("amount", amount).toString()
}