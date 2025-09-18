package org.jetbrains.research.libsl.ast.stmt

import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location

data class ExprStmt(
    override var location: Location?,
    var expr: Expr,
) : Stmt
