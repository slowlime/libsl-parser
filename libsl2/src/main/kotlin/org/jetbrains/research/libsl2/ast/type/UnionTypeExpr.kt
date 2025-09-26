package org.jetbrains.research.libsl2.ast.type

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location

data class UnionTypeExpr(
    override var location: Location?,
    var lhs: TypeExpr,
    var rhs: TypeExpr,
) : TypeExpr

fun UnionTypeExpr.walk(visitor: Visitor) {
    visitor.visit(lhs)
    visitor.visit(rhs)
}
