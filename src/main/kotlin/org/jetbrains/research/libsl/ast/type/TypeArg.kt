package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider

sealed interface TypeArg : LocationProvider {
    data class Wildcard(override val location: Location?) : TypeArg
}
