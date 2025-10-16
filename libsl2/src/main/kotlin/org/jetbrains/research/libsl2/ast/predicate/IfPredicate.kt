package org.jetbrains.research.libsl2.ast.predicate

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.expr.Expr
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.scope.MutableScope

data class IfPredicate(
    override var location: Location?,
    var condition: Expr,
    var thenBranch: Predicate,
    var elseBranch: Predicate?,
) : Predicate {
    lateinit var thenScope: MutableScope
    lateinit var elseScope: MutableScope
}

fun IfPredicate.walk(visitor: Visitor) {
    visitor.visit(condition)
    visitor.visit(thenBranch)
    elseBranch?.let(visitor::visit)
}
