package org.jetbrains.research.libsl2.ast.contract

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.expr.Expr
import org.jetbrains.research.libsl2.location.Location

data class AssignsContract(
    override var location: Location?,
    var name: Name?,
    var expr: Expr,
) : Contract

fun AssignsContract.walk(visitor: Visitor) {
    visitor.visit(expr)
}
