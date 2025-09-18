package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.location.Location

data class PointerTypeExpr(
    override var location: Location?,
    var base: TypeExpr,
) : TypeExpr
