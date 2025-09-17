package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class AnnotationDecl(
    override val location: Location?,
    val name: Name,
    val params: MutableList<Param>,
) : GlobalDecl {
    data class Param(val name: Name, val typeExpr: TypeExpr, val default: Expr?)
}
