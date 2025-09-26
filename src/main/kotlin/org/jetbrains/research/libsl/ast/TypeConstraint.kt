package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.type.TypeConstructor

data class TypeConstraint(
    var param: Name,
    var bound: TypeExpr,
) {
    lateinit var resolvedParam: Def<TypeConstructor>
}

fun TypeConstraint.walk(visitor: Visitor) {
    visitor.visit(bound)
}
