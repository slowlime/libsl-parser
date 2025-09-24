package org.jetbrains.research.libsl.ast.stmt

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location

data class AssignStmt(
    override var location: Location?,
    var lhs: Access,
    var inPlaceOp: InPlaceOp?,
    var rhs: Expr,
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

fun AssignStmt.walk(visitor: Visitor) {
    visitor.visit(lhs)
    visitor.visit(rhs)
}
