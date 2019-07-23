package io.rsbox.engine.action

import io.rsbox.engine.fs.def.AnimDef
import io.rsbox.engine.message.impl.SynthSoundMessage
import io.rsbox.engine.model.LockState
import io.rsbox.engine.model.Tile
import io.rsbox.engine.model.attr.KILLER_ATTR
import io.rsbox.engine.model.entity.Npc
import io.rsbox.engine.model.entity.Player
import io.rsbox.engine.model.npcdrops.NpcDropHandler
import io.rsbox.engine.model.queue.QueueTask
import io.rsbox.engine.model.queue.TaskPriority
import io.rsbox.engine.plugin.Plugin
import io.rsbox.engine.service.log.LoggerService
import java.lang.ref.WeakReference

/**
 * This class is responsible for handling npc death events.
 *
 * @author Tom <rspsmods@gmail.com>
 */
object NpcDeathAction {

    val deathPlugin: Plugin.() -> Unit = {
        val npc = ctx as Npc

        npc.interruptQueues()
        npc.stopMovement()
        npc.lock()

        npc.queue(TaskPriority.STRONG) {
            death(npc)
        }
    }

    private suspend fun QueueTask.death(npc: Npc) {
        val world = npc.world
        val deathAnimation = npc.combatDef.deathAnimation
        val deathSound = npc.combatDef.deathSound
        val respawnDelay = npc.combatDef.respawnDelay

        val deathTile: Tile = npc.tile

        npc.damageMap.getMostDamage()?.let { killer ->
            if (killer is Player) {
                world.getService(LoggerService::class.java, searchSubclasses = true)?.logNpcKill(killer, npc)
            }
            npc.attr[KILLER_ATTR] = WeakReference(killer)
        }

        val killer = npc.attr[KILLER_ATTR]?.get()

        if(killer is Player) {
            killer.write(SynthSoundMessage(deathSound, 1, 0))
        }

        world.plugins.executeNpcPreDeath(npc)

        npc.resetFacePawn()

        deathAnimation.forEach { anim ->
            val def = npc.world.definitions.get(AnimDef::class.java, anim)
            npc.animate(def.id)
            wait(def.cycleLength + 1)
        }

        npc.animate(-1)

        world.plugins.executeNpcDeath(npc)

        // Handle Drops
        if(killer is Player) {
            NpcDropHandler.processDrop(npc, killer, deathTile)
        }

        if (npc.respawns) {
            npc.invisible = true
            npc.reset()
            wait(respawnDelay)
            npc.invisible = false
            world.plugins.executeNpcSpawn(npc)
        } else {
            world.remove(npc)
        }
    }

    private fun Npc.reset() {
        lock = LockState.NONE
        tile = spawnTile
        setTransmogId(-1)

        attr.clear()
        timers.clear()
        world.setNpcDefaults(this)
    }
}