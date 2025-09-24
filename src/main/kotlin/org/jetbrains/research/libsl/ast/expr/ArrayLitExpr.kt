package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.Location

data class ArrayLitExpr(
    override var location: Location?,
    var elems: MutableList<Expr>,
) : Expr

fun ArrayLitExpr.walk(visitor: Visitor) {
    for (elem in elems) {
        visitor.visit(elem)
    }
}
