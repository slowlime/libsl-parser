package org.jetbrains.research.libsl.ast.contract

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location

data class AssignsContract(
    override var location: Location?,
    var name: Name?,
    var expr: Expr,
) : Contract

fun AssignsContract.walk(visitor: Visitor) {
    visitor.visit(expr)
}
