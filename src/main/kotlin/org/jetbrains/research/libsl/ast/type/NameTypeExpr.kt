package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.ast.FullName
import org.jetbrains.research.libsl.location.Location

data class NameTypeExpr(
    override val location: Location?,
    val typeName: FullName,
    val typeArgs: MutableList<TypeArg>?,
) : TypeExpr
