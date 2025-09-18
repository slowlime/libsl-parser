package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.location.Location

data class UnaryExpr(
    override var location: Location?,
    var op: Op,
    var rhs: Expr,
) : Expr {
    enum class Op {
        Plus,
        Neg,
        BitNot,
        Not,
    }
}
