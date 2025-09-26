package org.jetbrains.research.libsl2.ast.access

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.expr.Expr
import org.jetbrains.research.libsl2.location.Location

data class IndexAccess(
    override var location: Location?,
    var base: Access,
    var index: Expr,
) : Access

fun IndexAccess.walk(visitor: Visitor) {
    visitor.visit(base)
}
