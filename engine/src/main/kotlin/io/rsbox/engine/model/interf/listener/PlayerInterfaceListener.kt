package io.rsbox.engine.model.interf.listener

import io.rsbox.engine.model.entity.RSPlayer
import io.rsbox.engine.oldplugin.PluginRepository

/**
 * @author Tom <rspsmods@gmail.com>
 */
class PlayerInterfaceListener(val player: RSPlayer, val plugins: PluginRepository) : InterfaceListener {

    override fun onInterfaceOpen(interfaceId: Int) {
        plugins.executeInterfaceOpen(player, interfaceId)
    }

    override fun onInterfaceClose(interfaceId: Int) {
        player.world.plugins.executeInterfaceClose(player, interfaceId)
    }
}