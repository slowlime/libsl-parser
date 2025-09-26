package org.jetbrains.research.libsl2.ast.access

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location

data class FieldAccess(
    override var location: Location?,
    var base: Access,
    var field: Name,
) : Access

fun FieldAccess.walk(visitor: Visitor) {
    visitor.visit(base)
}
