package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.ast.type.TypeArg
import org.jetbrains.research.libsl.location.Location

data class ProcCallExpr(
    override val location: Location?,
    val callee: Access,
    val typeArgs: MutableList<TypeArg>?,
    val args: MutableList<Expr>,
) : Expr
