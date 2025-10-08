package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.Annotatable
import org.jetbrains.research.libsl2.ast.LibSLAnnotation
import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.QualifiedTypeName
import org.jetbrains.research.libsl2.ast.TypeConstraint
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.ast.walk
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity
import org.jetbrains.research.libsl2.resolve.scope.MutableScope

data class AutomatonDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var isConcept: Boolean,
    var name: QualifiedTypeName,
    var constructorVariables: MutableList<VariableDecl>,
    var typeExpr: TypeExpr,
    var implementedConcepts: MutableList<Name>,
    val typeConstraints: MutableList<TypeConstraint>,
    var decls: MutableList<AutomatonMemberDecl>
) : GlobalDecl, Annotatable, Entity<AutomatonDecl> {
    override lateinit var primaryDef: Def.Primary<AutomatonDecl>
    lateinit var scope: MutableScope
}

fun AutomatonDecl.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    for (variable in constructorVariables) {
        visitor.visit(variable)
    }

    visitor.visit(typeExpr)

    for (typeConstraint in typeConstraints) {
        typeConstraint.walk(visitor)
    }

    for (decl in decls) {
        visitor.visit(decl)
    }
}
