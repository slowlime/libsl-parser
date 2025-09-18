package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class AnnotationDecl(
    override var location: Location?,
    var name: Name,
    var params: MutableList<Param>,
) : GlobalDecl {
    data class Param(var name: Name, var typeExpr: TypeExpr, var default: Expr?)
}
