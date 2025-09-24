package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.Location

data class BinaryExpr(
    override var location: Location?,
    var lhs: Expr,
    var op: Op,
    var rhs: Expr,
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

fun BinaryExpr.walk(visitor: Visitor) {
    visitor.visit(lhs)
    visitor.visit(rhs)
}
