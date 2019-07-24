package io.rsbox.engine.action

import io.rsbox.engine.fs.def.AnimDef
import io.rsbox.api.KILLER_ATTR
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.api.TaskPriority
import io.rsbox.api.entity.Pawn
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.queue.RSQueueTask
import io.rsbox.engine.oldplugin.Plugin
import io.rsbox.engine.service.log.LoggerService
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
object PlayerDeathAction {

    private const val DEATH_ANIMATION = 836

    val deathPlugin: Plugin.() -> Unit = {
        val player = ctx as RSPlayer

        player.interruptQueues()
        player.stopMovement()
        player.lock()

        player.queue(TaskPriority.STRONG) { this as RSQueueTask
            death(player)
        }
    }

    private suspend fun RSQueueTask.death(player: RSPlayer) {
        val world = player.world
        val deathAnim = world.definitions.get(AnimDef::class.java, DEATH_ANIMATION)
        val instancedMap = world.instanceAllocator.getMap(player.tile as RSTile)

        player.damageMap.getMostDamage()?.let { killer ->
            if (killer is RSPlayer) {
                world.getService(LoggerService::class.java, searchSubclasses = true)?.logPlayerKill(killer, player)
            }
            player.attr[KILLER_ATTR] = WeakReference(killer as Pawn)
        }

        world.plugins.executePlayerPreDeath(player)

        player.resetFacePawn()
        wait(2)
        player.animate(deathAnim.id)
        wait(deathAnim.cycleLength + 1)
        player.getSkills().restoreAll()
        player.animate(-1)
        if (instancedMap == null) {
            // Note: maybe add a player attribute for death locations
            player.moveTo(player.world.gameContext.home)
        } else {
            player.moveTo(instancedMap.exitTile)
            world.instanceAllocator.death(player)
        }
        player.writeMessage("Oh dear, you are dead!")
        player.unlock()

        player.attr.removeIf { it.resetOnDeath }
        player.timers.removeIf { it.resetOnDeath }

        world.plugins.executePlayerDeath(player)
    }
}