package org.jetbrains.research.libsl.ast.stmt

import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location

data class AssignStmt(
    override val location: Location?,
    val lhs: Access,
    val inPlaceOp: InPlaceOp?,
    val rhs: Expr,
) : Stmt {
    enum class InPlaceOp {
        Add,
        Sub,
        Mul,
        Div,
        Mod,
        BitAnd,
        BitOr,
        BitXor,
        LShift,
        RShift,
    }
}
