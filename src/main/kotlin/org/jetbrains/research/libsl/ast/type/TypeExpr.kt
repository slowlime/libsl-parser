package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.LocationProvider

sealed interface TypeExpr : LocationProvider, TypeArg

fun TypeExpr.walk(visitor: Visitor) {
    when (this) {
        is IntersectionTypeExpr -> visitor.visit(this)
        is NameTypeExpr -> visitor.visit(this)
        is PointerTypeExpr -> visitor.visit(this)
        is PrimitiveLitTypeExpr -> visitor.visit(this)
        is UnionTypeExpr -> visitor.visit(this)
    }
}
