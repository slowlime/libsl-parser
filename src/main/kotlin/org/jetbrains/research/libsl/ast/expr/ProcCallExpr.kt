package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.ast.type.TypeArg
import org.jetbrains.research.libsl.location.Location

data class ProcCallExpr(
    override var location: Location?,
    var callee: Access,
    var typeArgs: MutableList<TypeArg>?,
    var args: MutableList<Expr>,
) : Expr
