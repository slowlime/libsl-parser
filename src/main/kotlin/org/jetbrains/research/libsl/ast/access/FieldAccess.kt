package org.jetbrains.research.libsl.ast.access

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.Location

data class FieldAccess(
    override var location: Location?,
    var base: Access,
    var field: Name,
) : Access

fun FieldAccess.walk(visitor: Visitor) {
    visitor.visit(base)
}
