package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.type.TypeConstructor

data class TypeConstraint(
    var param: Name,
    var bound: TypeExpr,
) {
    lateinit var resolvedParam: Def<TypeConstructor>
}

fun TypeConstraint.walk(visitor: Visitor) {
    visitor.visit(bound)
}
