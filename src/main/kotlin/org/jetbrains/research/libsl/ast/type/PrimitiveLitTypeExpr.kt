package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.ast.PrimitiveLit
import org.jetbrains.research.libsl.location.Location

data class PrimitiveLitTypeExpr(val lit: PrimitiveLit) : TypeExpr {
    override val location: Location? by lit::location
}
