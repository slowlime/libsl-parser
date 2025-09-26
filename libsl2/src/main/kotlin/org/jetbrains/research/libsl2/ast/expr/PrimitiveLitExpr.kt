package org.jetbrains.research.libsl2.ast.expr

import org.jetbrains.research.libsl2.ast.PrimitiveLit
import org.jetbrains.research.libsl2.location.Location

data class PrimitiveLitExpr(var lit: PrimitiveLit) : Expr {
    override var location: Location? by lit::location
}
