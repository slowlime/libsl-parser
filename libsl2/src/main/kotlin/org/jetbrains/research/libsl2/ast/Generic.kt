package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.location.LocationProvider
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity
import org.jetbrains.research.libsl2.type.TypeConstructor

data class Generic(
    var variance: Variance?,
    var name: Name,
) : LocationProvider,
    Entity<TypeConstructor> {
    override var location: Location? by name::location
    override lateinit var primaryDef: Def.Primary<TypeConstructor>
}
