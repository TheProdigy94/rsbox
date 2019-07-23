package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.TeleportMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.priv.Privilege

/**
 * @author Tom <rspsmods@gmail.com>
 */
class TeleportHandler : MessageHandler<TeleportMessage> {

    override fun handle(client: Client, world: RSWorld, message: TeleportMessage) {
        if (!client.lock.canMove()) {
            return
        }

        log(client, "Teleport world map: unknown=%d, x=%d, z=%d, height=%d", message.unknown, message.x, message.z, message.height)

        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        if (world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            client.moveTo(message.x, message.z, message.height)
        }
    }
}