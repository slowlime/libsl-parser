package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.location.Location

data class AccessExpr(var access: Access) : Expr {
    override var location: Location? by access::location
}

fun AccessExpr.walk(visitor: Visitor) {
    visitor.visit(access)
}
