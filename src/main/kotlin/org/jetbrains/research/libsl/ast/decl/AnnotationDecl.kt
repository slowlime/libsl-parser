package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity

data class AnnotationDecl(
    override var location: Location?,
    var name: Name,
    var params: MutableList<Param>,
) : GlobalDecl, Entity<AnnotationDecl> {
    data class Param(var name: Name, var typeExpr: TypeExpr, var default: Expr?)

    override lateinit var primaryDef: Def.Primary<AnnotationDecl>
}
