package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.ast.PrimitiveLit
import org.jetbrains.research.libsl.location.Location

data class PrimitiveLitTypeExpr(var lit: PrimitiveLit) : TypeExpr {
    override var location: Location? by lit::location
}
