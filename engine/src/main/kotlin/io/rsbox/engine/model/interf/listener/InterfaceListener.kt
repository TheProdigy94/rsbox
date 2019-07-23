package io.rsbox.engine.model.interf.listener

/**
 * @author Tom <rspsmods@gmail.com>
 */
interface InterfaceListener {

    fun onInterfaceOpen(interfaceId: Int)

    fun onInterfaceClose(interfaceId: Int)
}