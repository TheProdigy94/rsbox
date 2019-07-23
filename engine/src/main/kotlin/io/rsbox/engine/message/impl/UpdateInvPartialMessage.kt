package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message
import io.rsbox.engine.model.item.Item

/**
 * @author Tom <rspsmods@gmail.com>
 */
class UpdateInvPartialMessage(val oldItems: Array<Item?>, val newItems: Array<Item?>, val componentHash: Int, val containerKey: Int) : Message {

    constructor(interfaceId: Int, component: Int, containerKey: Int, oldItems: Array<Item?>, newItems: Array<Item?>) : this(oldItems, newItems, (interfaceId shl 16) or component, containerKey)

    constructor(interfaceId: Int, component: Int, oldItems: Array<Item?>, newItems: Array<Item?>) : this(oldItems, newItems, (interfaceId shl 16) or component, 0)

    constructor(containerKey: Int, oldItems: Array<Item?>, newItems: Array<Item?>) : this(oldItems, newItems, -1, containerKey)
}