package org.jetbrains.research.libsl2.ast.stmt

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.expr.Expr
import org.jetbrains.research.libsl2.location.Location

data class ExprStmt(
    override var location: Location?,
    var expr: Expr,
) : Stmt

fun ExprStmt.walk(visitor: Visitor) {
    visitor.visit(expr)
}
