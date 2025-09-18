package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class TypeCmpExpr(
    override var location: Location?,
    var lhs: Expr,
    var rhs: TypeExpr,
) : Expr
