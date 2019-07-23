package io.rsbox.api

import mu.KLogger

/**
 * @author Kyle Escobar
 */

interface Server {
    fun getWorld(): World

    fun getLogger(): KLogger
}