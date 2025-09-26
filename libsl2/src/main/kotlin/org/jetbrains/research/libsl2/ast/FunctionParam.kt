package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.resolve.Binding
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity

data class FunctionParam(
    override var annotations: MutableList<LibSLAnnotation>,
    var name: Name,
    var typeExpr: TypeExpr,
) : Annotatable,
    Entity<Binding> {
    override lateinit var primaryDef: Def.Primary<Binding>
}

fun FunctionParam.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    visitor.visit(typeExpr)
}
