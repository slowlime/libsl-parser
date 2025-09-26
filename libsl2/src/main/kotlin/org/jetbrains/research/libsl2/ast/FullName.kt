package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.location.Location

data class FullName(var components: MutableList<Name>) {
    init {
        require(components.isNotEmpty()) { "component list must be non-empty" }
    }

    val location: Location?
        get() = components.first().location

    override fun toString(): String = components.joinToString(".") { it.name }
}
