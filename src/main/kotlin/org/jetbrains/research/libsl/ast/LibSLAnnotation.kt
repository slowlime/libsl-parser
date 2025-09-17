package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider

data class LibSLAnnotation(
    override val location: Location?,
    val name: Name,
    val args: MutableList<Arg>
) : LocationProvider {
    data class Arg(
        val name: Name?,
        val expr: Expr,
    )
}
