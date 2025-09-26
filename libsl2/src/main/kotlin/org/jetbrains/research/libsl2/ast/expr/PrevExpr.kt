package org.jetbrains.research.libsl2.ast.expr

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.access.Access
import org.jetbrains.research.libsl2.location.Location

data class PrevExpr(
    override var location: Location?,
    var access: Access,
) : Expr

fun PrevExpr.walk(visitor: Visitor) {
    visitor.visit(access)
}
