package io.rsbox.engine.model.shop

import io.rsbox.engine.model.RSWorld
import io.rsbox.engine.model.entity.RSPlayer

/**
 * Represents the currency exchange when performing transactions in a [RSShop].
 *
 * @author Tom <rspsmods@gmail.com>
 */
interface ShopCurrency {

    /**
     * Called when a player selects the "value" option a [ShopItem].
     */
    fun onSellValueMessage(p: RSPlayer, shopItem: ShopItem)

    /**
     * Called when a player selects the "value" option on one of their own
     * inventory items.
     */
    fun onBuyValueMessage(p: RSPlayer, shop: RSShop, item: Int)

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
    fun sellToPlayer(p: RSPlayer, shop: RSShop, slot: Int, amt: Int)

    /**
     * Called when a player attempts to sell an inventory item to the shop.
     */
    fun buyFromPlayer(p: RSPlayer, shop: RSShop, slot: Int, amt: Int)
}