package io.rsbox.engine.model.container.key

import io.rsbox.engine.model.container.ContainerStackType

/**
 * A decoupled file that holds [ContainerKey]s that are pre-defined in our core
 * game-module.
 *
 * @author Tom <rspsmods@gmail.com>
 */

val INVENTORY_KEY = ContainerKey("inventory", capacity = 28, stackType = ContainerStackType.NORMAL)
val EQUIPMENT_KEY = ContainerKey("equipment", capacity = 14, stackType = ContainerStackType.NORMAL)
val BANK_KEY = ContainerKey("bank", capacity = 800, stackType = ContainerStackType.STACK)
val GE_RETURN_0 = ContainerKey("ge_returns_0", capacity = 2, stackType = ContainerStackType.STACK)
val GE_RETURN_1 = ContainerKey("ge_returns_1", capacity = 2, stackType = ContainerStackType.STACK)
val GE_RETURN_2 = ContainerKey("ge_returns_2", capacity = 2, stackType = ContainerStackType.STACK)
val GE_RETURN_3 = ContainerKey("ge_returns_3", capacity = 2, stackType = ContainerStackType.STACK)
val GE_RETURN_4 = ContainerKey("ge_returns_4", capacity = 2, stackType = ContainerStackType.STACK)
val GE_RETURN_5 = ContainerKey("ge_returns_5", capacity = 2, stackType = ContainerStackType.STACK)
val GE_RETURN_6 = ContainerKey("ge_returns_6", capacity = 2, stackType = ContainerStackType.STACK)
val GE_RETURN_7 = ContainerKey("ge_returns_7", capacity = 2, stackType = ContainerStackType.STACK)