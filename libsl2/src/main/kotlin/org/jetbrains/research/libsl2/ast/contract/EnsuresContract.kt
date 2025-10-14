package org.jetbrains.research.libsl2.ast.contract

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.predicate.Predicate
import org.jetbrains.research.libsl2.location.Location

data class EnsuresContract(
    override var location: Location?,
    var name: Name?,
    var predicate: Predicate,
) : Contract

fun EnsuresContract.walk(visitor: Visitor) {
    visitor.visit(predicate)
}
