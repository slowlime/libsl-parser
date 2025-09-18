package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.location.Location

data class UnionTypeExpr(
    override var location: Location?,
    var lhs: TypeExpr,
    var rhs: TypeExpr,
) : TypeExpr
