package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.EventCameraPositionMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSClient

/**
 * @author Tom <rspsmods@gmail.com>
 */
class EventCameraPositionHandler : MessageHandler<EventCameraPositionMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: EventCameraPositionMessage) {
        client.cameraPitch = message.pitch
        client.cameraYaw = message.yaw
    }
}