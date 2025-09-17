package org.jetbrains.research.libsl.ast.stmt

import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location

data class IfStmt(
    override val location: Location?,
    val condition: Expr,
    val thenBranch: MutableList<Stmt>,
    val elseBranch: MutableList<Stmt>?,
) : Stmt
