package org.jetbrains.research.libsl.ast.stmt

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location

data class IfStmt(
    override var location: Location?,
    var condition: Expr,
    var thenBranch: MutableList<Stmt>,
    var elseBranch: MutableList<Stmt>?,
) : Stmt

fun IfStmt.walk(visitor: Visitor) {
    visitor.visit(condition)

    for (stmt in thenBranch) {
        visitor.visit(stmt)
    }

    elseBranch?.forEach { visitor.visit(it) }
}
