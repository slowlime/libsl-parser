package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.type.TypeExpr

data class FunctionParam(
    override var annotations: MutableList<LibSLAnnotation>,
    var name: Name,
    var typeExpr: TypeExpr,
) : Annotatable

fun FunctionParam.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    visitor.visit(typeExpr)
}
