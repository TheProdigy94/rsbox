package io.rsbox.engine.model.entity

import io.rsbox.engine.model.Direction
import io.rsbox.engine.model.Tile

class NPCSpawn(npcId: Int, tile: Tile, walkRadius: Int, dir: Direction) {
    private val npcId: Int = npcId
    private val tile: Tile = tile
    private val walkRadius: Int = walkRadius
    private val dir: Direction = dir

    fun getId(): Int { return this.npcId }
    fun getTile(): Tile { return this.tile }
    fun getWalkRadius(): Int { return this.walkRadius }
    fun getDirection(): Direction { return this.dir }
}