package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.type.TypeExpr

data class FunctionSignature(
    var name: Name,
    var params: MutableList<TypeExpr>?,
)

fun FunctionSignature.walk(visitor: Visitor) {
    params?.forEach { visitor.visit(it) }
}
