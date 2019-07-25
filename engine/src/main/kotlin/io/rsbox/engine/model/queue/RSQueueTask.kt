package io.rsbox.engine.model.queue

import io.rsbox.api.*
import io.rsbox.api.entity.Npc
import io.rsbox.api.entity.Pawn
import io.rsbox.api.entity.Player
import io.rsbox.engine.message.impl.ResumePauseButtonMessage
import io.rsbox.engine.model.entity.RSNpc
import io.rsbox.engine.model.entity.RSPawn
import io.rsbox.engine.model.entity.RSPlayer
import mu.KLogging
import kotlin.coroutines.*

/**
 * Represents a task that can be paused, or suspended, and resumed at any point
 * in the future.
 *
 * @author Tom <rspsmods@gmail.com>
 */
data class RSQueueTask(val ctx: Any, val priority: TaskPriority) : Continuation<Unit>, QueueTask {

    lateinit var coroutine: Continuation<Unit>

    /**
     * If the task's logic has already been invoked.
     */
    var invoked = false

    /**
     * A value that can be requested by a task, such as an input for dialogs.
     */
    var requestReturnValue: Any? = null

    /**
     * Represents an action that should be executed if, and only if, this task
     * was terminated via [terminate].
     */
    var terminateAction: ((QueueTask).() -> Unit)? = null

    /**
     * The next [SuspendableStep], if any, that must be handled once a [SuspendableCondition]
     * returns [SuspendableCondition.resume] as true.
     */
    private var nextStep: SuspendableStep? = null

    /**
     * The [CoroutineContext] implementation for our task.
     */
    override val context: CoroutineContext = EmptyCoroutineContext

    override val pawn: Pawn get() = ctx as RSPawn

    override val player: Player get() = ctx as RSPlayer

    override val npc: Npc get() = ctx as RSNpc

    /**
     * When the [nextStep] [SuspendableCondition.resume] returns true, this
     * method is called.
     */
    override fun resumeWith(result: Result<Unit>) {
        nextStep = null
        result.exceptionOrNull()?.let { e -> logger.error("Error with oldplugin!", e) }
    }

    /**
     * The logic in each [SuspendableStep] must be game-thread-safe, so we use
     * this method to keep them in-sync.
     */
    internal fun cycle() {
        val next = nextStep ?: return

        if (next.condition.resume()) {
            next.continuation.resume(Unit)
            requestReturnValue = null
        }
    }

    /**
     * Terminate any further execution of this task, during any state,
     * and invoke [terminateAction] if applicable (not null).
     */
    fun terminate() {
        nextStep = null
        requestReturnValue = null
        terminateAction?.invoke(this)
    }

    /**
     * If the task has been "paused" (aka suspended).
     */
    fun suspended(): Boolean = nextStep != null

    /**
     * Wait for the specified amount of game cycles [cycles] before
     * continuing the logic associated with this task.
     */
    suspend fun wait(cycles: Int): Unit = suspendCoroutine {
        check(cycles > 0) { "Wait cycles must be greater than 0." }
        nextStep = SuspendableStep(WaitCondition(cycles), it)
    }

    /**
     * Wait for [predicate] to return true.
     */
    suspend fun wait(predicate: () -> Boolean): Unit = suspendCoroutine {
        nextStep = SuspendableStep(
            PredicateCondition { predicate() },
            it
        )
    }

    /**
     * Wait for our [ctx] to reach [tile]. Note that [ctx] MUST be an instance
     * of [RSPawn] and that the height of the [tile] and [RSPawn.tile] must be equal,
     * as well as the x and z coordinates.
     */
    suspend fun waitTile(tile: Tile): Unit = suspendCoroutine {
        nextStep = SuspendableStep(
            TileCondition(
                (ctx as RSPawn).tile,
                tile
            ), it
        )
    }

    /**
     * Wait for our [ctx] as [RSPlayer] to close the [interfaceId].
     */
    suspend fun waitInterfaceClose(interfaceId: Int): Unit = suspendCoroutine {
        nextStep = SuspendableStep(PredicateCondition {
            !(ctx as RSPlayer).interfaces.isVisible(interfaceId)
        }, it)
    }

    /**
     * Wait for <strong>any</strong> return value to be available before
     * continuing.
     */
    suspend fun waitReturnValue(): Unit = suspendCoroutine {
        nextStep = SuspendableStep(
            PredicateCondition { requestReturnValue != null },
            it
        )
    }

    ////////////////// API ////////////////////////////

    /**
     * The child id of the chat box in the gameframe interface. This can change
     * with revision.
     */
    private val CHATBOX_CHILD = 561

    /**
     * The id for the appearance interface.
     */
    private val APPEARANCE_INTERFACE_ID = 269

    override val closeAppearance: QueueTask.() -> Unit = { this as RSQueueTask
        player.closeInterface(APPEARANCE_INTERFACE_ID)
    }

    override suspend fun selectAppearance(): Appearance? {
        player
        player.openInterface(APPEARANCE_INTERFACE_ID, InterfaceDestination.MAIN_SCREEN)

        terminateAction = closeAppearance
        waitReturnValue()
        terminateAction!!(this)

        return requestReturnValue as? Appearance
    }

    /**
     * The default action that will occur when interrupting or finishing a dialog.
     */
    private val closeDialog: QueueTask.() -> Unit = {
        player.closeComponent(parent = 162, child = CHATBOX_CHILD)
    }

    /**
     * Prompts the player with options.
     *
     * @return
     * The id of the option chosen. The id can range from [1] inclusive to [options.size] inclusive.
     */
    suspend fun options(vararg options: String, title: String = "Select an Option"): Int {
        player.sendTempVarbit(5983, 1)
        player.runClientScript(2379)
        player.openInterface(parent = 162, child = CHATBOX_CHILD, interfaceId = 219)
        player.runClientScript(58, title, options.joinToString("|"))
        player.setInterfaceEvents(interfaceId = 219, component = 1, from = 1, to = options.size, setting = 1)

        terminateAction = closeDialog
        waitReturnValue()
        terminateAction!!(this)

        return (requestReturnValue as? ResumePauseButtonMessage)?.slot ?: -1
    }

    ///////////////////////////////////////////////////

    override fun equals(other: Any?): Boolean {
        val o = other as? RSQueueTask ?: return false
        return super.equals(o) && o.coroutine == coroutine
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + coroutine.hashCode()
        return result
    }

    class EmptyReturnValue

    companion object : KLogging() {
        val EMPTY_RETURN_VALUE = EmptyReturnValue()
    }
}