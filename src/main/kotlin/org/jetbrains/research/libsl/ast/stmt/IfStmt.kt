package org.jetbrains.research.libsl.ast.stmt

import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location

data class IfStmt(
    override var location: Location?,
    var condition: Expr,
    var thenBranch: MutableList<Stmt>,
    var elseBranch: MutableList<Stmt>?,
) : Stmt
