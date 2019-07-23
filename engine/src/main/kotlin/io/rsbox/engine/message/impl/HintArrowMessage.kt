package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

data class HintArrowMessage(val arrow_type: Int, val index_or_x: Int, val arrow_y: Int, val offset_z: Int): Message