package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.location.Location

data class ArrayLitExpr(
    override val location: Location?,
    val elems: MutableList<Expr>,
) : Expr
