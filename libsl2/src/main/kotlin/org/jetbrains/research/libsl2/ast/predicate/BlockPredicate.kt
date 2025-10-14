package org.jetbrains.research.libsl2.ast.predicate

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location

data class BlockPredicate(
    override var location: Location?,
    var predicates: MutableList<Predicate>,
) : Predicate

fun BlockPredicate.walk(visitor: Visitor) {
    for (predicate in predicates) {
        visitor.visit(predicate)
    }
}
