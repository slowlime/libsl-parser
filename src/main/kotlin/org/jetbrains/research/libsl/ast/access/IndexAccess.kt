package org.jetbrains.research.libsl.ast.access

import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location

data class IndexAccess(
    override val location: Location?,
    val base: Access,
    val index: Expr,
) : Access
