package org.jetbrains.research.libsl2.ast.expr

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.location.Location

data class CastExpr(
    override var location: Location?,
    var lhs: Expr,
    var rhs: TypeExpr,
) : Expr

fun CastExpr.walk(visitor: Visitor) {
    visitor.visit(lhs)
    visitor.visit(rhs)
}
