package org.jetbrains.research.libsl2.ast.expr

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.access.Access
import org.jetbrains.research.libsl2.location.Location

data class HasConceptExpr(
    override var location: Location?,
    var lhs: Access,
    var concept: Name,
) : Expr

fun HasConceptExpr.walk(visitor: Visitor) {
    visitor.visit(lhs)
}
