package org.jetbrains.research.libsl2.ast.expr

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location

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

fun UnaryExpr.walk(visitor: Visitor) {
    visitor.visit(rhs)
}
