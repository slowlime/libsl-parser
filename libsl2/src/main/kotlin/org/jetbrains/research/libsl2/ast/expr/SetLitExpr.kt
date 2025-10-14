package org.jetbrains.research.libsl2.ast.expr

import org.jetbrains.research.libsl2.location.Location

data class SetLitExpr(
    override var location: Location?,
    var elems: MutableList<Expr>,
) : Expr
