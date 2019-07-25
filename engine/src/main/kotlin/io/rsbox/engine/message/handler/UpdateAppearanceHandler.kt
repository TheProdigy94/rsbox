package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.UpdateAppearanceMessage
import io.rsbox.engine.model.RSAppearance
import io.rsbox.engine.model.RSGender
import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSClient
import java.util.Arrays

/**
 * @author Tom <rspsmods@gmail.com>
 */
class UpdateAppearanceHandler : MessageHandler<UpdateAppearanceMessage> {

    override fun handle(client: RSClient, world: RSWorld, message: UpdateAppearanceMessage) {
        val gender = if (message.gender == 1) RSGender.FEMALE else RSGender.MALE
        val looks = message.appearance
        val colors = message.colors

        log(client, "Update appearance: gender=%s, appearance=%s, colors=%s", gender.toString(), Arrays.toString(looks), Arrays.toString(colors))
        client.queues.submitReturnValue(RSAppearance(looks, colors, gender))
    }
}