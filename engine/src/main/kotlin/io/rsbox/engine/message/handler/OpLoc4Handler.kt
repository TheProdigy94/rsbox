package io.rsbox.engine.message.handler

import io.rsbox.engine.action.ObjectPathAction
import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.OpLoc4Message
import io.rsbox.engine.model.EntityType
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld
import io.rsbox.api.INTERACTING_OBJ_ATTR
import io.rsbox.api.INTERACTING_OPT_ATTR
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.entity.RSGameObject
import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.model.priv.Privilege
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpLoc4Handler : MessageHandler<OpLoc4Message> {

    override fun handle(client: Client, world: RSWorld, message: OpLoc4Message) {
        /*
         * If tile is too far away, don't process it.
         */
        val tile = RSTile(message.x, message.z, client.tile.height)
        if (!tile.viewableFrom(client.tile, RSPlayer.TILE_VIEW_DISTANCE)) {
            return
        }

        /*
         * If player can't move, we don't do anything.
         */
        if (!client.lock.canMove()) {
            return
        }

        /*
         * Get the region chunk that the object would belong to.
         */
        val chunk = world.chunks.getOrCreate(tile)
        val obj = chunk.getEntities<RSGameObject>(tile, EntityType.STATIC_OBJECT, EntityType.DYNAMIC_OBJECT).firstOrNull { it.id == message.id } ?: return

        log(client, "Object action 4: id=%d, x=%d, z=%d, movement=%d", message.id, message.x, message.z, message.movementType)

        client.stopMovement()
        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        if (message.movementType == 1 && world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            val def = obj.getDef(world.definitions)
            client.moveTo(world.findRandomTileAround(obj.tile, radius = 1, centreWidth = def.width, centreLength = def.length) ?: obj.tile)
        }

        client.attr[INTERACTING_OPT_ATTR] = 4
        client.attr[INTERACTING_OBJ_ATTR] = WeakReference(obj)
        client.executePlugin(ObjectPathAction.objectInteractPlugin)
    }
}