package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.type.TypeExpr

data class FunctionParam(
    override val annotations: MutableList<LibSLAnnotation>,
    val name: Name,
    val typeExpr: TypeExpr,
) : Annotatable
