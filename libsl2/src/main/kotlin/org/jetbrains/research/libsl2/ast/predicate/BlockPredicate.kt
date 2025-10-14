package org.jetbrains.research.libsl2.ast.predicate

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.scope.MutableScope

data class BlockPredicate(
    override var location: Location?,
    var predicates: MutableList<Predicate>,
) : Predicate {
    lateinit var scope: MutableScope
}

fun BlockPredicate.walk(visitor: Visitor) {
    for (predicate in predicates) {
        visitor.visit(predicate)
    }
}
