package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message
import io.rsbox.engine.model.item.Item

/**
 * @author Tom <rspsmods@gmail.com>
 */
class UpdateInvFullMessage(val items: Array<Item?>, val componentHash: Int, val containerKey: Int) : Message {

    constructor(interfaceId: Int, component: Int, containerKey: Int, items: Array<Item?>) : this(items, (interfaceId shl 16) or component, containerKey)

    constructor(interfaceId: Int, component: Int, items: Array<Item?>) : this(items, (interfaceId shl 16) or component, 0)

    constructor(containerKey: Int, items: Array<Item?>) : this(items, -1, containerKey)
}