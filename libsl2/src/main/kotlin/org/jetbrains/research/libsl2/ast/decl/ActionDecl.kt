package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.Annotatable
import org.jetbrains.research.libsl2.ast.Generic
import org.jetbrains.research.libsl2.ast.LibSLAnnotation
import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.TypeConstraint
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.ast.walk
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Binding
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity
import org.jetbrains.research.libsl2.resolve.scope.MutableScope

class ActionDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var name: Name,
    var generics: MutableList<Generic>,
    var params: MutableList<Param>,
    var returnType: TypeExpr?,
    var typeConstraints: MutableList<TypeConstraint>,
) : GlobalDecl, Annotatable, Entity<ActionDecl> {
    data class Param(
        override var annotations: MutableList<LibSLAnnotation>,
        var name: Name,
        var typeExpr: TypeExpr,
    ) : Annotatable, Entity<Binding> {
        override lateinit var primaryDef: Def.Primary<Binding>
    }

    override lateinit var primaryDef: Def.Primary<ActionDecl>
    lateinit var paramScope: MutableScope
}

fun ActionDecl.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    for (param in params) {
        param.walk(visitor)
    }

    returnType?.let(visitor::visit)

    for (typeConstraint in typeConstraints) {
        typeConstraint.walk(visitor)
    }
}

fun ActionDecl.Param.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    visitor.visit(typeExpr)
}
