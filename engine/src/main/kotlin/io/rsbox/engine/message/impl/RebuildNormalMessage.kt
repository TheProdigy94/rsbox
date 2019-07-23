package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message
import io.rsbox.engine.service.xtea.XteaKeyService

/**
 * @author Tom <rspsmods@gmail.com>
 */
class RebuildNormalMessage(val x: Int, val z: Int, val xteaKeyService: XteaKeyService?) : Message