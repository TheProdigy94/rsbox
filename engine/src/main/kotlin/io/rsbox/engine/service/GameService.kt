package io.rsbox.engine.service

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.rsbox.engine.RSServer
import io.rsbox.engine.message.MessageDecoderSet
import io.rsbox.engine.message.MessageEncoderSet
import io.rsbox.engine.message.MessageStructureSet
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.task.*
import io.rsbox.engine.task.parallel.ParallelNpcCycleTask
import io.rsbox.engine.task.parallel.ParallelPlayerCycleTask
import io.rsbox.engine.task.parallel.ParallelPlayerPostCycleTask
import io.rsbox.engine.task.parallel.ParallelSynchronizationTask
import io.rsbox.engine.task.sequential.SequentialNpcCycleTask
import io.rsbox.engine.task.sequential.SequentialPlayerCycleTask
import io.rsbox.engine.task.sequential.SequentialPlayerPostCycleTask
import io.rsbox.engine.task.sequential.SequentialSynchronizationTask
import io.rsbox.util.ServerProperties
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import mu.KLogging
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * The service used to schedule and execute logic needed for the game to run properly.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class GameService : Service {

    /**
     * The associated world with our current game.
     */
    lateinit var world: RSWorld

    /**
     * The max amount of incoming [io.rsbox.engine.message.Message]s that can be
     * handled per cycle.
     */
    var maxMessagesPerCycle = 0

    /**
     * The scheduler for our game cycle logic as well as coroutine dispatcher.
     */
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
            ThreadFactoryBuilder()
                    .setNameFormat("game-context")
                    .setUncaughtExceptionHandler { t, e -> logger.error("Error with thread $t", e) }
                    .build())

    /**
     * A list of jobs that will be executed on the next cycle after being
     * submitted.
     */
    private val gameThreadJobs = ConcurrentLinkedQueue<() -> Unit>()

    /**
     * The amount of ticks that have gone by since the last debug log.
     */
    private var debugTick = 0

    /**
     * The total time, in milliseconds, that the past [TICKS_PER_DEBUG_LOG]
     * cycles have taken to complete.
     */
    private var cycleTime = 0

    /**
     * The Kotlin Coroutine dispatcher to submit suspendable plugins.
     */
    val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

    /**
     * The amount of time, in milliseconds, that each [GameTask] has taken away
     * from the game cycle.
     */
    private val taskTimes = Object2LongOpenHashMap<Class<GameTask>>()

    /**
     * The amount of time, in milliseconds, that [SequentialPlayerCycleTask]
     * has taken for each [io.rsbox.engine.model.entity.RSPlayer].
     */
    internal val playerTimes = Object2LongOpenHashMap<String>()

    /**
     * The amount of active [io.rsbox.engine.model.queue.QueueTask]s throughout
     * the [io.rsbox.engine.model.entity.RSPlayer]s.
     */
    internal var totalPlayerQueues = 0

    /**
     * The amount of active [io.rsbox.engine.model.queue.QueueTask]s throughout
     * the [io.rsbox.engine.model.entity.RSNpc]s.
     */
    internal var totalNpcQueues = 0

    /**
     * The amount of active [io.rsbox.engine.model.queue.QueueTask]s throughout
     * the [io.rsbox.engine.model.RSWorld].
     */
    internal var totalWorldQueues = 0

    /**
     * A list of tasks that will be executed per game cycle.
     */
    private val tasks = mutableListOf<GameTask>()

    internal val messageStructures = MessageStructureSet()

    internal val messageEncoders = MessageEncoderSet()

    internal val messageDecoders = MessageDecoderSet()

    /**
     * This flag indicates that the game cycles should pause.
     *
     * Should not be used without proper knowledge of how it works!
     */
    internal var pause = false

    override fun init(server: RSServer, world: RSWorld, serviceProperties: ServerProperties) {
        this.world = world
        populateTasks(serviceProperties)
        maxMessagesPerCycle = serviceProperties.getOrDefault("messages-per-cycle", 30)
        executor.scheduleAtFixedRate(this::cycle, 0, world.gameContext.cycleTime.toLong(), TimeUnit.MILLISECONDS)
    }

    override fun postLoad(server: RSServer, world: RSWorld) {
    }

    override fun terminate(server: RSServer, world: RSWorld) {
    }

    private fun populateTasks(serviceProperties: ServerProperties) {
        /*
         * Determine which synchronization task we're going to use based on the
         * number of available processors we have been provided, also taking
         * into account the amount of processors the machine has in the first
         * place.
         */
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        val processors = Math.max(1, Math.min(availableProcessors, serviceProperties.getOrDefault("processors", availableProcessors)))
        val sequentialTasks = processors == 1 || serviceProperties.getOrDefault("sequential-tasks", false)

        if (sequentialTasks) {
            tasks.addAll(arrayOf(
                    MessageHandlerTask(),
                    QueueHandlerTask(),
                    SequentialPlayerCycleTask(),
                    ChunkCreationTask(),
                    WorldRemoveTask(),
                    SequentialNpcCycleTask(),
                    SequentialSynchronizationTask(),
                    SequentialPlayerPostCycleTask()
            ))
            logger.info("Sequential tasks preference enabled. {} tasks will be handled per cycle.", tasks.size)
        } else {
            val executor = Executors.newFixedThreadPool(processors, ThreadFactoryBuilder()
                    .setNameFormat("game-task-thread")
                    .setUncaughtExceptionHandler { t, e -> logger.error("Error with thread $t", e) }
                    .build())

            tasks.addAll(arrayOf(
                    MessageHandlerTask(),
                    QueueHandlerTask(),
                    ParallelPlayerCycleTask(executor),
                    ChunkCreationTask(),
                    WorldRemoveTask(),
                    ParallelNpcCycleTask(executor),
                    ParallelSynchronizationTask(executor),
                    ParallelPlayerPostCycleTask(executor)
            ))
            logger.info("Parallel tasks preference enabled. {} tasks will be handled per cycle.", tasks.size)
        }
    }

    override fun bindNet(server: RSServer, world: RSWorld) {
    }

    /**
     * Submits a job that must be performed on the game-thread.
     */
    fun submitGameThreadJob(job: Function0<Unit>) {
        gameThreadJobs.offer(job)
    }

    private fun cycle() {
        if (pause) {
            return
        }
        val start = System.currentTimeMillis()

        /*
         * Clear the time it has taken to complete [GameTask]s from last cycle.
         */
        taskTimes.clear()
        playerTimes.clear()

        /*
         * Execute any logic jobs that were submitted.
         */
        gameThreadJobs.forEach { job ->
            try {
                job()
            } catch (e: Exception) {
                logger.error("Error executing game-thread job.", e)
            }
        }
        /*
         * Reset the logic jobs as they have been completed.
         */
        gameThreadJobs.clear()

        /*
         * Go over the [tasks] and execute their logic. Log the time it took
         * each [GameTask] to complete. Some of the tasks may also calculate
         * their time for each player so that we can have the amount of time,
         * in milliseconds, that each player took to perform certain tasks.
         */
        tasks.forEach { task ->
            val taskStart = System.currentTimeMillis()
            try {
                task.execute(world, this)
            } catch (e: Exception) {
                logger.error("Error with task ${task.javaClass.simpleName}.", e)
            }
            taskTimes[task.javaClass] = System.currentTimeMillis() - taskStart
        }

        world.cycle()

        /*
         * Calculate the time, in milliseconds, it took for this cycle to complete
         * and add it to [cycleTime].
         */
        cycleTime += (System.currentTimeMillis() - start).toInt()

        if (debugTick++ >= TICKS_PER_DEBUG_LOG) {
            val freeMemory = Runtime.getRuntime().freeMemory()
            val totalMemory = Runtime.getRuntime().totalMemory()
            val maxMemory = Runtime.getRuntime().maxMemory()

            /*
             * Description:
             *
             * Cycle time:
             * the average time it took for a game cycle to
             * complete the last [TICKS_PER_DEBUG_LOG] game cycles.
             *
             * Entities:
             * The amount of entities in the world.
             * p: players
             * n: npcs
             *
             * Map:
             * The amount of map entities that are currently active.
             * c: chunks [io.rsbox.engine.model.region.Chunk]
             * r: regions
             * i: instanced maps [io.rsbox.engine.model.instance.InstancedMap]
             *
             * Queues:
             * The amount of plugins that are being executed on this exact
             * game cycle.
             * p: players
             * n: npcs
             * w: world
             *
             * Mem Usage:
             * Memory usage statistics.
             * U: used memory, in megabytes
             * R: reserved memory, in megabytes
             * M: max memory available, in megabytes
             */
            logger.info("[Cycle time: {}ms] [Entities: {}p / {}n] [Map: {}c / {}r / {}i] [Queues: {}p / {}n / {}w] [Mem usage: U={}MB / R={}MB / M={}MB].",
                    cycleTime / TICKS_PER_DEBUG_LOG, world.players.count(), world.npcs.count(),
                    world.chunks.getActiveChunkCount(), world.chunks.getActiveRegionCount(), world.instanceAllocator.activeMapCount,
                    totalPlayerQueues, totalNpcQueues, totalWorldQueues,
                    (totalMemory - freeMemory) / (1024 * 1024), totalMemory / (1024 * 1024), maxMemory / (1024 * 1024))
            debugTick = 0
            cycleTime = 0
        }

        val freeTime = world.gameContext.cycleTime - (System.currentTimeMillis() - start)
        if (freeTime < 0) {
            /*
             * If the cycle took more than [GameContext.cycleTime]ms, we log the
             * occurrence as well as the time each [GameTask] took to complete,
             * as well as how long each [io.rsbox.engine.model.entity.RSPlayer] took
             * to process this cycle.
             */
            logger.error { "Cycle took longer than expected: ${(-freeTime) + world.gameContext.cycleTime}ms / ${world.gameContext.cycleTime}ms!" }
            logger.error { taskTimes.toList().sortedByDescending { (_, value) -> value }.toMap() }
            logger.error { playerTimes.toList().sortedByDescending { (_, value) -> value }.toMap() }
        }
    }

    companion object : KLogging() {

        /**
         * The amount of ticks that must go by for debug info to be logged.
         */
        private const val TICKS_PER_DEBUG_LOG = 10
    }
}
