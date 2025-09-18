package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.PrimitiveLit
import org.jetbrains.research.libsl.location.Location

data class PrimitiveLitExpr(var lit: PrimitiveLit) : Expr {
    override var location: Location? by lit::location
}
