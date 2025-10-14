package org.jetbrains.research.libsl2.ast.predicate

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Binding
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity

data class NamedPredicate(
    override var location: Location?,
    var name: Name,
    var predicate: Predicate,
) : Predicate, Entity<Binding> {
    override lateinit var primaryDef: Def.Primary<Binding>
}

fun NamedPredicate.walk(visitor: Visitor) {
    visitor.visit(predicate)
}
