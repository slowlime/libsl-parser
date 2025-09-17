package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.location.Location

data class UnaryExpr(
    override val location: Location?,
    val op: Op,
    val rhs: Expr,
) : Expr {
    enum class Op {
        Plus,
        Neg,
        BitNot,
        Not,
    }
}
