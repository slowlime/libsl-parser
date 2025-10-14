package org.jetbrains.research.libsl2.ast.predicate

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.expr.Expr
import org.jetbrains.research.libsl2.location.Location

data class ExprPredicate(
    override var location: Location?,
    var expr: Expr,
) : Predicate

fun ExprPredicate.walk(visitor: Visitor) {
    visitor.visit(expr)
}
