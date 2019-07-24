package io.rsbox.engine.action

import io.rsbox.api.KILLER_ATTR
import io.rsbox.engine.model.LockState
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.entity.RSNpc
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.api.TaskPriority
import io.rsbox.api.entity.Pawn
import io.rsbox.engine.fs.def.AnimDef
import io.rsbox.engine.message.impl.SynthSoundMessage
import io.rsbox.engine.model.npcdrops.NpcDropHandler
import io.rsbox.engine.model.queue.RSQueueTask
import io.rsbox.engine.oldplugin.Plugin
import io.rsbox.engine.service.log.LoggerService
import java.lang.ref.WeakReference

/**
 * This class is responsible for handling npc death events.
 *
 * @author Tom <rspsmods@gmail.com>
 */
object NpcDeathAction {

    val deathPlugin: Plugin.() -> Unit = {
        val npc = ctx as RSNpc

        npc.interruptQueues()
        npc.stopMovement()
        npc.lock()

        npc.queue(TaskPriority.STRONG) { this as RSQueueTask
            death(npc)
        }
    }

    private suspend fun RSQueueTask.death(npc: RSNpc) {
        val world = npc.world
        val deathAnimation = npc.combatDef.deathAnimation
        val deathSound = npc.combatDef.deathSound
        val respawnDelay = npc.combatDef.respawnDelay

        val deathTile: RSTile = npc.tile as RSTile

        npc.damageMap.getMostDamage()?.let { killer ->
            if (killer is RSPlayer) {
                world.getService(LoggerService::class.java, searchSubclasses = true)?.logNpcKill(killer, npc)
            }
            npc.attr[KILLER_ATTR] = WeakReference(killer as Pawn)
        }

        val killer = npc.attr[KILLER_ATTR]?.get()

        if(killer is RSPlayer) {
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
        if(killer is RSPlayer) {
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

    private fun RSNpc.reset() {
        lock = LockState.NONE
        tile = spawnTile
        setTransmogId(-1)

        attr.clear()
        timers.clear()
        world.setNpcDefaults(this)
    }
}