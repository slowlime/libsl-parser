package org.jetbrains.research.libsl2.ast.type

import org.jetbrains.research.libsl2.ast.PrimitiveLit
import org.jetbrains.research.libsl2.location.Location

data class PrimitiveLitTypeExpr(var lit: PrimitiveLit) : TypeExpr {
    override var location: Location? by lit::location
}
