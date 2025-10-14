package org.jetbrains.research.libsl2.ast.predicate

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location

data class NamedPredicate(
    override var location: Location?,
    var name: Name,
    var predicate: Predicate,
) : Predicate

fun NamedPredicate.walk(visitor: Visitor) {
    visitor.visit(predicate)
}
