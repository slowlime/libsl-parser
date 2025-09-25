package org.jetbrains.research.libsl.ast.stmt

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.scope.MutableScope

data class IfStmt(
    override var location: Location?,
    var condition: Expr,
    var thenBranch: MutableList<Stmt>,
    var elseBranch: MutableList<Stmt>?,
) : Stmt {
    lateinit var thenScope: MutableScope
    lateinit var elseScope: MutableScope
}

fun IfStmt.walk(visitor: Visitor) {
    visitor.visit(condition)

    for (stmt in thenBranch) {
        visitor.visit(stmt)
    }

    elseBranch?.forEach { visitor.visit(it) }
}
