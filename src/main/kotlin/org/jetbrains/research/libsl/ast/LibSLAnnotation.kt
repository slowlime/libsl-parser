package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider

data class LibSLAnnotation(
    override var location: Location?,
    var name: Name,
    var args: MutableList<Arg>
) : LocationProvider {
    data class Arg(
        var name: Name?,
        var expr: Expr,
    )
}
