package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.type.TypeArg
import org.jetbrains.research.libsl.location.Location

data class ActionCallExpr(
    override val location: Location?,
    val name: Name,
    val typeArgs: MutableList<TypeArg>?,
    val args: MutableList<Expr>,
) : Expr
