package org.jetbrains.research.libsl2.ast.expr

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.access.Access
import org.jetbrains.research.libsl2.ast.type.TypeArg
import org.jetbrains.research.libsl2.location.Location

data class ProcCallExpr(
    override var location: Location?,
    var callee: Access,
    var typeArgs: MutableList<TypeArg>?,
    var args: MutableList<Expr>,
) : Expr

fun ProcCallExpr.walk(visitor: Visitor) {
    visitor.visit(callee)
    typeArgs?.forEach { visitor.visit(it) }

    for (arg in args) {
        visitor.visit(arg)
    }
}
