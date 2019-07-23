package io.rsbox.engine.message.handler

import io.rsbox.engine.message.MessageHandler
import io.rsbox.engine.message.impl.ClientCheatMessage
import io.rsbox.engine.model.World
import io.rsbox.engine.model.entity.Client
import io.rsbox.engine.service.log.LoggerService
import java.util.Arrays

/**
 * @author Tom <rspsmods@gmail.com>
 */
class ClientCheatHandler : MessageHandler<ClientCheatMessage> {

    override fun handle(client: Client, world: World, message: ClientCheatMessage) {
        val values = message.command.split(" ")
        val command = values[0].toLowerCase()
        val args = if (values.size > 1) values.slice(1 until values.size).filter { it.isNotEmpty() }.toTypedArray() else null

        log(client, "Command: cmd=%s, args=%s", command, Arrays.toString(args ?: emptyArray<String>()))

        val handled = world.plugins.executeCommand(client, command, args)
        if (handled) {
            world.getService(LoggerService::class.java, searchSubclasses = true)?.logCommand(client, command, *args ?: emptyArray())
        } else {
            client.writeMessage("No valid command found: $command")
        }
    }
}