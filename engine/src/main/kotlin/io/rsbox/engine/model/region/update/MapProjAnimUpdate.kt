package io.rsbox.engine.model.region.update

import io.rsbox.engine.message.Message
import io.rsbox.engine.message.impl.MapProjAnimMessage
import io.rsbox.engine.model.entity.Projectile

/**
 * Represents an update where a [Projectile] is spawned.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class MapProjAnimUpdate(override val type: EntityUpdateType,
                        override val entity: Projectile) : EntityUpdate<Projectile>(type, entity) {

    override fun toMessage(): Message = if (entity.targetPawn != null) {
        val targetIndex = if (entity.targetPawn.entityType.isNpc) entity.targetPawn.index + 1 else -(entity.targetPawn.index + 1)
        MapProjAnimMessage(
                start = ((entity.tile.x and 0x7) shl 4) or (entity.tile.z and 0x7),
                pawnTargetIndex = targetIndex, offsetX = entity.targetTile.x - entity.tile.x, offsetZ = entity.targetTile.z - entity.tile.z,
                gfx = entity.gfx, startHeight = entity.startHeight, endHeight = entity.endHeight,
                delay = entity.delay, lifespan = entity.lifespan, angle = entity.angle, steepness = entity.steepness)
    } else {
        MapProjAnimMessage(
                start = ((entity.tile.x and 0x7) shl 4) or (entity.tile.z and 0x7),
                pawnTargetIndex = 0, offsetX = entity.targetTile.x - entity.tile.x, offsetZ = entity.targetTile.z - entity.tile.z,
                gfx = entity.gfx, startHeight = entity.startHeight, endHeight = entity.endHeight, delay = entity.delay,
                lifespan = entity.lifespan, angle = entity.angle, steepness = entity.steepness)
    }
}