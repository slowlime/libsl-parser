package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity
import org.jetbrains.research.libsl.type.Type

data class Generic(
    var variance: Variance?,
    var name: Name,
) : LocationProvider, Entity<Type> {
    override var location: Location? by name::location
    override lateinit var primaryDef: Def.Primary<Type>
}
