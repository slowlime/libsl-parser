package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.location.Location

data class HasConceptExpr(
    override var location: Location?,
    var lhs: Access,
    var concept: Name,
) : Expr

fun HasConceptExpr.walk(visitor: Visitor) {
    visitor.visit(lhs)
}
