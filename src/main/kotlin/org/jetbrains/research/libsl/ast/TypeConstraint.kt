package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.type.TypeArg
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.type.Type

data class TypeConstraint(
    var param: Name,
    var variance: Variance?,
    var bound: TypeArg,
) {
    lateinit var resolvedParam: Def<Type>
}

fun TypeConstraint.walk(visitor: Visitor) {
    visitor.visit(bound)
}
