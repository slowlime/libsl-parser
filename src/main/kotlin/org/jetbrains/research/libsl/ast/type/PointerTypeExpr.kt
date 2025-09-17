package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.location.Location

data class PointerTypeExpr(
    override val location: Location?,
    val base: TypeExpr,
) : TypeExpr
