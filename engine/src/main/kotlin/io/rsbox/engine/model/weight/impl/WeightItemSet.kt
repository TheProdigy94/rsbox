package io.rsbox.engine.model.weight.impl

import io.rsbox.engine.model.item.Item
import io.rsbox.engine.model.weight.WeightNode
import io.rsbox.engine.model.weight.WeightNodeSet

/**
 * @author Tom <rspsmods@gmail.com>
 */
class WeightItemSet : WeightNodeSet<Item>() {

    override fun add(node: WeightNode<Item>): WeightItemSet {
        super.add(node)
        return this
    }
}