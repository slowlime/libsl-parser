package org.jetbrains.research.libsl.resolve

import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider
import org.jetbrains.research.libsl.resolve.scope.Scope

/**
 * An entity definition in a [Scope].
 */
interface Def<out T> : LocationProvider {
    val scope: Scope
    val name: String
    val primary: Primary<T>

    val entity: T
        get() = primary.entity

    fun isPrimary(): Boolean
    fun isAlias(): Boolean

    /**
     * A primary definition of an entity.
     */
    class Primary<out T>(
        override val scope: Scope,
        override val name: String,
        override var location: Location?,
        override val entity: T,
    ) : Def<T> {
        override val primary: Primary<T>
            get() = this

        override fun isPrimary(): Boolean = true
        override fun isAlias(): Boolean = false
    }

    /**
     * An alias referring to another definition.
     */
    class Alias<out T>(
        override val scope: Scope,
        override val name: String,
        override var location: Location?,
        val def: Def<T>,
    ) : Def<T> {
        override val primary: Primary<T> = def.primary

        override fun isPrimary(): Boolean = false
        override fun isAlias(): Boolean = true
    }
}
