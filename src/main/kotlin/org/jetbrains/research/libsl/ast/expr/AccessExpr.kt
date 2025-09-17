package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.location.Location

data class AccessExpr(val access: Access) : Expr {
    override val location: Location? by access::location
}
