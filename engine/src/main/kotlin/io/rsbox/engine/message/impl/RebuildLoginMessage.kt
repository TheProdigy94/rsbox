package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message
import io.rsbox.engine.model.Tile
import io.rsbox.engine.service.xtea.XteaKeyService

/**
 * @author Tom <rspsmods@gmail.com>
 */
class RebuildLoginMessage(val playerIndex: Int, val tile: Tile, val playerTiles: IntArray, val xteaKeyService: XteaKeyService?) : Message