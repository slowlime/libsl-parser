package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.resolve.Binding
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity

data class FunctionParam(
    override var annotations: MutableList<LibSLAnnotation>,
    var name: Name,
    var typeExpr: TypeExpr,
) : Annotatable, Entity<Binding> {
    override lateinit var primaryDef: Def.Primary<Binding>
}

fun FunctionParam.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    visitor.visit(typeExpr)
}
