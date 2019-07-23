package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.MoveGameClickMessage
import io.rsbox.engine.message.impl.SetMapFlagMessage
import io.rsbox.engine.model.MovementQueue
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.attr.NO_CLIP_ATTR
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.entity.Entity
import io.rsbox.engine.model.priv.Privilege
import io.rsbox.engine.model.timer.STUN_TIMER

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ClickMapHandler : MessageHandler<MoveGameClickMessage> {

    override fun handle(client: Client, world: RSWorld, message: MoveGameClickMessage) {
        if (!client.lock.canMove()) {
            return
        }

        if (client.timers.has(STUN_TIMER)) {
            client.write(SetMapFlagMessage(255, 255))
            client.writeMessage(Entity.YOURE_STUNNED)
            return
        }

        log(client, "Click map: x=%d, z=%d, type=%d", message.x, message.z, message.movementType)

        client.closeInterfaceModal()
        client.interruptQueues()
        client.resetInteractions()

        if (message.movementType == 2 && world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            client.moveTo(message.x, message.z, client.tile.height)
        } else {
            val stepType = if (message.movementType == 1) MovementQueue.StepType.FORCED_RUN else MovementQueue.StepType.NORMAL
            val noClip = client.attr[NO_CLIP_ATTR] ?: false
            client.walkTo(message.x, message.z, stepType, detectCollision = !noClip)
        }
    }
}