package org.jetbrains.research.libsl2.ast.predicate

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location

data class IfPredicate(
    override var location: Location?,
    var condition: Predicate,
    var thenBranch: Predicate,
    var elseBranch: Predicate?,
) : Predicate

fun IfPredicate.walk(visitor: Visitor) {
    visitor.visit(condition)
    visitor.visit(thenBranch)
    elseBranch?.let(visitor::visit)
}
