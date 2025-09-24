package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider

sealed interface TypeArg : LocationProvider {
    data class Wildcard(override var location: Location?) : TypeArg
}

fun TypeArg.walk(visitor: Visitor) {
    when (this) {
        is TypeArg.Wildcard -> {}
        is TypeExpr -> visitor.visit(this)
    }
}
