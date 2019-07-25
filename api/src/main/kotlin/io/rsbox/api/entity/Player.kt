package io.rsbox.api.entity

import io.rsbox.api.*
import io.rsbox.api.entity.Pawn


/**
 * Object which represents a player
 *
 * @author Kyle Escobar
 */
interface Player : Pawn {
    /**
     * The display name of the player.
     * NOT the login name.
     */
    var username: String

    /**
     * The appearance of the player
     */
    fun getAppearance(): Appearance

    fun setAppearance(looks: Appearance)

    fun closeInterface(interfaceId: Int)

    fun closeComponent(parent: Int, child: Int)

    fun getInterfaceAt(dest: InterfaceDestination): Int

    fun openOverlayInterface(displayMode: DisplayMode)

    fun openInterface(dest: InterfaceDestination, autoClose: Boolean = false)

    fun openInterface(parent: Int, child: Int, interfaceId: Int, type: Int = 0, isModal: Boolean = false)

    fun openInterface(interfaceId: Int, dest: InterfaceDestination, fullscreen: Boolean = false)

    fun closeInterface(dest: InterfaceDestination)

    fun addBlock(block: UpdateBlockType)

    fun hasBlock(block: UpdateBlockType): Boolean

    /**
     * Sends a game message to the player chatbox
     */
    fun message(message: String, type: ChatMessageType = ChatMessageType.CONSOLE, username: String? = null)

    /**
     * Sends a game message to the player chatbox that can be filtered out.
     */
    fun filterableMessage(message: String)
}