package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.MapBuildCompleteMessage
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSClient

/**
 * @author Tom <rspsmods@gmail.com>
 */
class MapBuildCompleteHandler : MessageHandler<MapBuildCompleteMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: MapBuildCompleteMessage) {
        client.lastMapBuildTime = world.currentCycle
    }
}