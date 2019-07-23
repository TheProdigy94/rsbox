package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

data class SetCameraPosMessage(val cameraX: Int, val cameraZ: Int, val cameraY: Int, val field4: Int, val field5: Int): Message