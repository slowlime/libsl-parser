package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.PrimitiveLit
import org.jetbrains.research.libsl.location.Location

data class PrimitiveLitExpr(val lit: PrimitiveLit) : Expr {
    override val location: Location? by lit::location
}
