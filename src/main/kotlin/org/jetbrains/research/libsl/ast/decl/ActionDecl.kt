package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.Generic
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.TypeConstraint
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.ast.walk
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Binding
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity
import org.jetbrains.research.libsl.resolve.scope.MutableScope

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
