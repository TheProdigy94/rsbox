package io.rsbox.engine.model.entity

import io.rsbox.engine.model.Direction
import io.rsbox.engine.model.RSTile

class NPCSpawn(npcId: Int, tile: RSTile, walkRadius: Int, dir: Direction) {
    private val npcId: Int = npcId
    private val tile: RSTile = tile
    private val walkRadius: Int = walkRadius
    private val dir: Direction = dir

    fun getId(): Int { return this.npcId }
    fun getTile(): RSTile { return this.tile }
    fun getWalkRadius(): Int { return this.walkRadius }
    fun getDirection(): Direction { return this.dir }
}