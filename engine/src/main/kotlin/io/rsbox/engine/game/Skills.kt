package io.rsbox.engine.game

import io.rsbox.engine.fs.def.EnumDef
import io.rsbox.engine.model.RSWorld

/**
 * @author Kyle Escobar
 */

object Skills {

    const val ATTACK = 0
    const val DEFENCE = 1
    const val STRENGTH = 2
    const val HITPOINTS = 3
    const val RANGED = 4
    const val PRAYER = 5
    const val MAGIC = 6
    const val COOKING = 7
    const val WOODCUTTING = 8
    const val FLETCHING = 9
    const val FISHING = 10
    const val FIREMAKING = 11
    const val CRAFTING = 12
    const val SMITHING = 13
    const val MINING = 14
    const val HERBLORE = 15
    const val AGILITY = 16
    const val THIEVING = 17
    const val SLAYER = 18
    const val FARMING = 19
    const val RUNECRAFTING = 20
    const val HUNTER = 21
    const val CONSTRUCTION = 22

    const val MIN_COMBAT_LVL = 3
    const val MAX_COMBAT_LVL = 126

    fun getSkillName(world: RSWorld, skill: Int): String {
        val enum = world.definitions.get(EnumDef::class.java, 680)
        return enum.getString(skill)
    }

    fun isCombat(skill: Int): Boolean = when (skill) {
        ATTACK, DEFENCE, HITPOINTS, STRENGTH,
        RANGED, PRAYER, MAGIC -> true
        else -> false
    }

    fun getSkillForName(world: RSWorld, maxSkills: Int, skillName: String): Int {
        for (i in 0 until maxSkills) {
            if (getSkillName(world, i).toLowerCase() == skillName) {
                return i
            }
        }
        return -1
    }
}