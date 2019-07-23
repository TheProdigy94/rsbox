package io.rsbox.engine.model.npcdrops

import io.rsbox.engine.model.item.Item

class NPCDropEntry(itemId: Int, min: Int, max: Int) {
    private val itemId = itemId
    private val min = min
    private val max = max

    fun getItemId(): Int { return this.itemId }
    fun getMin(): Int { return this.min }
    fun getMax(): Int { return this.max }

    fun getItem(): Item {
        var count: Int = 0
        if(min == max) {
            count = max
        } else {
            count = (min..max).random()
        }

        return Item(this.itemId, count)
    }
}