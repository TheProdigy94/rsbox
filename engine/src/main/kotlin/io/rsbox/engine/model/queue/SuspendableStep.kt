package io.rsbox.engine.model.queue

import kotlin.coroutines.Continuation

/**
 * A step in suspendable logic that can be used to step through oldplugin logic.
 *
 * @author Tom <rspsmods@gmail.com>
 */
data class SuspendableStep(val condition: SuspendableCondition, val continuation: Continuation<Unit>)