package io.rsbox.engine.message.impl

import io.rsbox.engine.message.Message

data class SetCameraAngleMessage(val localX: Int, val localZ: Int, val localY: Int, val slowdownSpeed: Int, val speed: Int) : Message