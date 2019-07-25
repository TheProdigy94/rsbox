package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.ClickWorldMapMessage
import io.rsbox.engine.model.RSTile
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSClient
import io.rsbox.engine.model.priv.Privilege

/**
 * @author HolyRSPS <dagreenrs@gmail.com>
 */
class ClickWorldMapHandler : MessageHandler<ClickWorldMapMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: ClickWorldMapMessage) {
        if (world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            log(client, "Click world map: %s", RSTile.from30BitHash(message.data).toString())
            client.moveTo(RSTile.from30BitHash(message.data))
        }
    }
}