package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.Visitor
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

fun AnnotationDecl.walk(visitor: Visitor) {
    for (param in params) {
        param.walk(visitor)
    }
}

fun AnnotationDecl.Param.walk(visitor: Visitor) {
    visitor.visit(typeExpr)
    default?.let(visitor::visit)
}
