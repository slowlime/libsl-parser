package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.type.TypeExpr

data class QualifiedFunctionName(
    val name: Name,
    val params: MutableList<TypeExpr>?,
)
