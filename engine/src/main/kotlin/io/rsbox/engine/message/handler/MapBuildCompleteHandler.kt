package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.MapBuildCompleteMessage
import io.rsbox.engine.model.World
import io.rsbox.engine.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class MapBuildCompleteHandler : MessageHandler<MapBuildCompleteMessage> {

    override fun handle(client: Client, world: World, message: MapBuildCompleteMessage) {
        client.lastMapBuildTime = world.currentCycle
    }
}