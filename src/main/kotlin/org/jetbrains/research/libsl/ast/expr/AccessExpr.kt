package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.location.Location

data class AccessExpr(var access: Access) : Expr {
    override var location: Location? by access::location
}
