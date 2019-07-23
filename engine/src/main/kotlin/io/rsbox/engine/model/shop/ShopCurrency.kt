package io.rsbox.engine.model.shop

import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.Player

/**
 * Represents the currency exchange when performing transactions in a [Shop].
 *
 * @author Tom <rspsmods@gmail.com>
 */
interface ShopCurrency {

    /**
     * Called when a player selects the "value" option a [ShopItem].
     */
    fun onSellValueMessage(p: Player, shopItem: ShopItem)

    /**
     * Called when a player selects the "value" option on one of their own
     * inventory items.
     */
    fun onBuyValueMessage(p: Player, shop: Shop, item: Int)

    /**
     * Get the price at which the shop will sell [item] for.
     */
    fun getSellPrice(world: RSWorld, item: Int): Int

    /**
     * Get the price at which the shop will buy [item] for.
     */
    fun getBuyPrice(world: RSWorld, item: Int): Int

    /**
     * Called when a player attempts to buy a [ShopItem].
     */
    fun sellToPlayer(p: Player, shop: Shop, slot: Int, amt: Int)

    /**
     * Called when a player attempts to sell an inventory item to the shop.
     */
    fun buyFromPlayer(p: Player, shop: Shop, slot: Int, amt: Int)
}