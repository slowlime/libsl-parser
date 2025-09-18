package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.ast.FullName
import org.jetbrains.research.libsl.location.Location

data class NameTypeExpr(
    override var location: Location?,
    var typeName: FullName,
    var typeArgs: MutableList<TypeArg>?,
) : TypeExpr
