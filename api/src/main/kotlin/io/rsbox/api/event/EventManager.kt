package io.rsbox.api.event

import java.lang.IllegalStateException

/**
 * @author Kyle Escobar
 */

object EventManager {
    fun callEvent(event: Event) {
        if(event.isAsync()) {
            if(Thread.holdsLock(this)) {
                throw IllegalStateException("${event.getEventName()} cannot be triggered asynchronously from inside synchronized code.")
            }
            this.fireEvent(event)
        } else {
            synchronized(this) {
                this.fireEvent(event)
            }
        }
    }

    private fun fireEvent(event: Event) {
        println("Events not finished yet.")
    }
}