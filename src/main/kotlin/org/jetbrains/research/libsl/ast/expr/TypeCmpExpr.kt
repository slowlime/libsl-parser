package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class TypeCmpExpr(
    override val location: Location?,
    val lhs: Expr,
    val rhs: TypeExpr,
) : Expr
