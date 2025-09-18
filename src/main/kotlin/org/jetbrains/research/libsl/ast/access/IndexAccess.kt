package org.jetbrains.research.libsl.ast.access

import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location

data class IndexAccess(
    override var location: Location?,
    var base: Access,
    var index: Expr,
) : Access
