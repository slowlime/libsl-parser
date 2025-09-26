package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.Annotatable
import org.jetbrains.research.libsl2.ast.LibSLAnnotation
import org.jetbrains.research.libsl2.ast.QualifiedTypeName
import org.jetbrains.research.libsl2.ast.TypeConstraint
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.ast.walk
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity
import org.jetbrains.research.libsl2.resolve.scope.MutableScope
import org.jetbrains.research.libsl2.type.TypeConstructor

data class StructDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var typeName: QualifiedTypeName,
    var isType: TypeExpr?,
    var forTypes: MutableList<TypeExpr>,
    var typeConstraints: MutableList<TypeConstraint>,
    var decls: MutableList<StructMemberDecl>,
) : GlobalDecl, Annotatable, Entity<TypeConstructor> {
    override lateinit var primaryDef: Def.Primary<TypeConstructor>
    lateinit var scope: MutableScope
}

fun StructDecl.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    isType?.let(visitor::visit)

    for (type in forTypes) {
        visitor.visit(type)
    }

    for (typeConstraint in typeConstraints) {
        typeConstraint.walk(visitor)
    }

    for (decl in decls) {
        visitor.visit(decl)
    }
}
