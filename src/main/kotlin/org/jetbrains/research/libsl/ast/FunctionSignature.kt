package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.decl.FunctionDecl
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.resolve.Def

data class FunctionSignature(
    var name: Name,
    var params: MutableList<TypeExpr>?,
) {
    lateinit var resolved: Def<FunctionDecl>
}

fun FunctionSignature.walk(visitor: Visitor) {
    params?.forEach { visitor.visit(it) }
}
