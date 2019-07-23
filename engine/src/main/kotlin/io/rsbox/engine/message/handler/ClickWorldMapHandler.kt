package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.ClickWorldMapMessage
import io.rsbox.engine.model.Tile
import io.rsbox.engine.model.World
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.model.priv.Privilege

/**
 * @author HolyRSPS <dagreenrs@gmail.com>
 */
class ClickWorldMapHandler : MessageHandler<ClickWorldMapMessage> {

    override fun handle(client: Client, world: World, message: ClickWorldMapMessage) {
        if (world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            log(client, "Click world map: %s", Tile.from30BitHash(message.data).toString())
            client.moveTo(Tile.from30BitHash(message.data))
        }
    }
}