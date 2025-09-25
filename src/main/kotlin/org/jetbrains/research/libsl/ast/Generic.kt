package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity
import org.jetbrains.research.libsl.type.TypeConstructor

data class Generic(
    var variance: Variance?,
    var name: Name,
) : LocationProvider, Entity<TypeConstructor> {
    override var location: Location? by name::location
    override lateinit var primaryDef: Def.Primary<TypeConstructor>
}
