package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.UpdateAppearanceMessage
import io.rsbox.engine.model.Appearance
import io.rsbox.engine.model.Gender
import io.rsbox.engine.model.World
import io.rsbox.engine.model.entity.Client
import java.util.Arrays

/**
 * @author Tom <rspsmods@gmail.com>
 */
class UpdateAppearanceHandler : MessageHandler<UpdateAppearanceMessage> {

    override fun handle(client: Client, world: World, message: UpdateAppearanceMessage) {
        val gender = if (message.gender == 1) Gender.FEMALE else Gender.MALE
        val looks = message.appearance
        val colors = message.colors

        log(client, "Update appearance: gender=%s, appearance=%s, colors=%s", gender.toString(), Arrays.toString(looks), Arrays.toString(colors))
        client.queues.submitReturnValue(Appearance(looks, colors, gender))
    }
}