package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.type.TypeArg

data class TypeConstraint(
    var param: Name,
    var variance: Variance?,
    var bound: TypeArg,
) {
    fun walk(visitor: Visitor) {
        visitor.visit(bound)
    }
}
