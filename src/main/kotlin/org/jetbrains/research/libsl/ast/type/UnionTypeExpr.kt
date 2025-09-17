package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.location.Location

data class UnionTypeExpr(
    override val location: Location?,
    val lhs: TypeExpr,
    val rhs: TypeExpr,
) : TypeExpr
