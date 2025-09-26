package org.jetbrains.research.libsl2.ast.expr

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.access.Access
import org.jetbrains.research.libsl2.location.Location

data class AccessExpr(var access: Access) : Expr {
    override var location: Location? by access::location
}

fun AccessExpr.walk(visitor: Visitor) {
    visitor.visit(access)
}
