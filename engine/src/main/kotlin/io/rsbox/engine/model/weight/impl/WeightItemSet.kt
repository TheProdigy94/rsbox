package io.rsbox.engine.model.weight.impl

import io.rsbox.engine.model.item.RSItem
import io.rsbox.engine.model.weight.WeightNode
import io.rsbox.engine.model.weight.WeightNodeSet

/**
 * @author Tom <rspsmods@gmail.com>
 */
class WeightItemSet : WeightNodeSet<RSItem>() {

    override fun add(node: WeightNode<RSItem>): WeightItemSet {
        super.add(node)
        return this
    }
}