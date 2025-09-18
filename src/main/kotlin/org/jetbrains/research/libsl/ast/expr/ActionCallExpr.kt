package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.type.TypeArg
import org.jetbrains.research.libsl.location.Location

data class ActionCallExpr(
    override var location: Location?,
    var name: Name,
    var typeArgs: MutableList<TypeArg>?,
    var args: MutableList<Expr>,
) : Expr
