package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.type.TypeArg

data class TypeConstraint(
    val param: Name,
    val variance: Variance?,
    val bound: TypeArg,
)
