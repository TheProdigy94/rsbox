package io.rsbox.api.plugin

/**
 * @author Kyle Escobar
 */

abstract class PluginBase : Plugin {
    override fun equals(other: Any?): Boolean {
        if(this === other) { return true }
        if(other == null) { return false }
        if(other !is Plugin) { return false }
        return getName().equals(other.getName())
    }

    override fun getName(): String {
        return getProperties().getName()
    }

    override fun hashCode(): Int {
        return getName().hashCode()
    }
}