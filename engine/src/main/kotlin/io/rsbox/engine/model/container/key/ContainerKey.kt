package io.rsbox.engine.model.container.key

import io.rsbox.engine.model.container.ContainerStackType

/**
 * A unique key used for an [io.rsbox.engine.model.container.ItemContainer].
 *
 * @author Tom <rspsmods@gmail.com>
 */
data class ContainerKey(val name: String, val capacity: Int, val stackType: ContainerStackType) {

    override fun equals(other: Any?): Boolean = (other as? ContainerKey)?.name == name

    override fun hashCode(): Int = name.hashCode()
}