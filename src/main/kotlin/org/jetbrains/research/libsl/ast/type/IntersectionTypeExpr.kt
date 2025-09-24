package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.Location

data class IntersectionTypeExpr(
    override var location: Location?,
    var lhs: TypeExpr,
    var rhs: TypeExpr,
) : TypeExpr

fun IntersectionTypeExpr.walk(visitor: Visitor) {
    visitor.visit(lhs)
    visitor.visit(rhs)
}
