package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.expr.Expr
import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.location.LocationProvider
import org.jetbrains.research.libsl2.resolve.Binding
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity
import org.jetbrains.research.libsl2.resolve.scope.MutableScope

data class AnnotationDecl(
    override var location: Location?,
    var name: Name,
    var params: MutableList<Param>,
) : GlobalDecl, Entity<AnnotationDecl> {
    data class Param(var name: Name, var typeExpr: TypeExpr, var default: Expr?) : LocationProvider, Entity<Binding> {
        override var location: Location? by name::location
        override lateinit var primaryDef: Def.Primary<Binding>
    }

    override lateinit var primaryDef: Def.Primary<AnnotationDecl>
    lateinit var paramScope: MutableScope
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
