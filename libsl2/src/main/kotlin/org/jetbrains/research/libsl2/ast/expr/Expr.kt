package org.jetbrains.research.libsl2.ast.expr

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.LocationProvider

sealed interface Expr : LocationProvider

fun Expr.walk(visitor: Visitor) {
    when (this) {
        is AccessExpr -> visitor.visit(this)
        is ActionCallExpr -> visitor.visit(this)
        is ArrayLitExpr -> visitor.visit(this)
        is SetLitExpr -> visitor.visit(this)
        is BinaryExpr -> visitor.visit(this)
        is CastExpr -> visitor.visit(this)
        is HasConceptExpr -> visitor.visit(this)
        is InstantiationExpr -> visitor.visit(this)
        is PrevExpr -> visitor.visit(this)
        is PrimitiveLitExpr -> visitor.visit(this)
        is ProcCallExpr -> visitor.visit(this)
        is TypeCmpExpr -> visitor.visit(this)
        is UnaryExpr -> visitor.visit(this)
    }
}
