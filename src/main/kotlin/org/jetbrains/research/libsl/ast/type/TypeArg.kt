package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.ast.Variance
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider

sealed interface TypeArg : LocationProvider {
    data class Wildcard(override var location: Location?) : TypeArg

    data class TypeExpr(
        override var location: Location?,
        var variance: Variance?,
        var typeExpr: org.jetbrains.research.libsl.ast.type.TypeExpr,
    ) : TypeArg
}

fun TypeArg.walk(visitor: Visitor) {
    when (this) {
        is TypeArg.Wildcard -> {}
        is TypeArg.TypeExpr -> visitor.visit(this.typeExpr)
    }
}
