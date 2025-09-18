package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.location.Location

data class PrevExpr(
    override var location: Location?,
    var access: Access,
) : Expr
