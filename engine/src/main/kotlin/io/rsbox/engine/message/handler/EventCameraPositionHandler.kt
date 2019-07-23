package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.EventCameraPositionMessage
import io.rsbox.engine.model.World
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class EventCameraPositionHandler : MessageHandler<EventCameraPositionMessage> {

    override fun handle(client: Client, world: World, message: EventCameraPositionMessage) {
        client.cameraPitch = message.pitch
        client.cameraYaw = message.yaw
    }
}