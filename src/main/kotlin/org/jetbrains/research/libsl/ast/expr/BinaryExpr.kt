package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.location.Location

data class BinaryExpr(
    override val location: Location?,
    val lhs: Expr,
    val op: Op,
    val rhs: Expr,
) : Expr {
    enum class Op {
        Mul,
        Div,
        Mod,
        Add,
        Sub,
        Sal,
        Sar,
        Shl,
        Shr,
        BitOr,
        BitXor,
        BitAnd,
        Lt,
        Le,
        Gt,
        Ge,
        Eq,
        Ne,
        Or,
        And,
    }
}
