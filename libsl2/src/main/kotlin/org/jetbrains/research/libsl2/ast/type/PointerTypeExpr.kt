package org.jetbrains.research.libsl2.ast.type

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location

data class PointerTypeExpr(
    override var location: Location?,
    var base: TypeExpr,
) : TypeExpr

fun PointerTypeExpr.walk(visitor: Visitor) {
    visitor.visit(base)
}
