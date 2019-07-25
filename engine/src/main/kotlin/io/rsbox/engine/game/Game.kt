package io.rsbox.engine.game

import io.rsbox.api.INTERACTING_ITEM_SLOT
import io.rsbox.api.OTHER_ITEM_SLOT_ATTR
import io.rsbox.engine.model.entity.RSClient
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.api.InterfaceDestination

/**
 * @author Kyle Escobar
 */

object Game {
    fun closeModal(player: RSPlayer) {
        val modal = player.interfaces.getModal()
        if(modal != -1) {
            player.closeInterfaceModal()
            player.interfaces.setModal(-1)
        }
    }

    fun openMenuCheck(player: RSPlayer): Boolean {
        return player.getInterfaceAt(dest = InterfaceDestination.MAIN_SCREEN) != -1
    }

    fun login(player: RSPlayer) {
        // Skill-related logic.
        if (player.getSkills().getMaxLevel(Skills.HITPOINTS) < 10) {
            player.getSkills().setBaseLevel(Skills.HITPOINTS, 10)
        }
        player.calculateAndSetCombatLevel()
        player.sendWeaponComponentInformation()
        player.setComponentText(593, 2, "Combat Lvl: ${player.combatLevel}")


        // Interface-related logic.
        player.openOverlayInterface(player.interfaces.displayMode)
        InterfaceDestination.values.filter { pane -> pane.interfaceId != -1 }.forEach { pane ->
            if (pane == InterfaceDestination.XP_COUNTER && player.getVarbit(OSRSGameFrame.XP_DROPS_VISIBLE_VARBIT) == 0) {
                return@forEach
            } else if (pane == InterfaceDestination.MINI_MAP && player.getVarbit(OSRSGameFrame.HIDE_DATA_ORBS_VARBIT) == 1) {
                return@forEach
            }

            player.openInterface(pane.interfaceId, pane)
        }

        // Inform the client whether or not we have a display name.
        val displayName = player.username.isNotBlank()
        player.runClientScript(1105, if (displayName) 1 else 0) // Has display name
        player.runClientScript(423, player.username)
        if (player.getVarp(1055) == 0 && displayName) {
            player.syncVarp(1055)
        }
        player.setVarbit(8119, 1) // Has display name

        // Sync attack priority options.
        player.syncVarp(OSRSGameFrame.NPC_ATTACK_PRIORITY_VARP)
        player.syncVarp(OSRSGameFrame.PLAYER_ATTACK_PRIORITY_VARP)

        // Send player interaction options.
        player.addOption("Follow", 3)
        player.addOption("Trade with", 4)
        player.addOption("Report", 5)

        // Game-related logic.
        player.sendRunEnergy(player.runEnergy.toInt())

    }

    fun swapItem(client: RSClient) {
        val srcSlot = client.attr[INTERACTING_ITEM_SLOT]!!
        val dstSlot = client.attr[OTHER_ITEM_SLOT_ATTR]!!

        val container = (client as RSPlayer).inventory

        if(srcSlot in 0 until container.capacity && dstSlot in 0 until container.capacity) {
            container.swap(srcSlot, dstSlot)
        } else {
            container.dirty = true
        }
    }
}