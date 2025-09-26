package org.jetbrains.research.libsl2.ast.expr

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location

data class ArrayLitExpr(
    override var location: Location?,
    var elems: MutableList<Expr>,
) : Expr

fun ArrayLitExpr.walk(visitor: Visitor) {
    for (elem in elems) {
        visitor.visit(elem)
    }
}
