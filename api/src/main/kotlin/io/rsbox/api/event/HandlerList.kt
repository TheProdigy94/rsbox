package io.rsbox.api.event

import io.rsbox.api.plugin.RegisteredListener
import java.util.*
import kotlin.collections.ArrayList

/**
 * A list of event handlers, stored per-event. Based on lahwrans's fevents.
 * @author Kyle Escobar
 */
class HandlerList {
    @Volatile
    private lateinit var handlers: Array<RegisteredListener>

    @Volatile
    private var handlerslots: EnumMap<EventPriority, ArrayList<RegisteredListener>>

    private val allLists: ArrayList<HandlerList> = arrayListOf()

    init {
        handlerslots = EnumMap<EventPriority, ArrayList<RegisteredListener>>(EventPriority::class.java)
        for(o in EventPriority.values()) {
            handlerslots.put(o, arrayListOf())
        }
        synchronized(allLists) {
            allLists.add(this)
        }
    }
}