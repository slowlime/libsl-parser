package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.Annotation
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

class ActionDecl(
    override val location: Location?,
    override val annotations: MutableList<Annotation>,
    val name: Name,
    val generics: MutableList<Generic>,
    val params: MutableList<Param>,
    val returnType: TypeExpr?,
    val typeConstraints: MutableList<TypeConstraint>,
) : GlobalDecl, Annotatable {
    data class Param(
        override val annotations: MutableList<Annotation>,
        val name: Name,
        val typeExpr: TypeExpr,
    ) : Annotatable
}
