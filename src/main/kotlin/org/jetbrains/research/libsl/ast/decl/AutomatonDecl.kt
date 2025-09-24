package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity
import org.jetbrains.research.libsl.resolve.scope.MutableScope

data class AutomatonDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var isConcept: Boolean,
    var name: QualifiedTypeName,
    var constructorVariables: MutableList<VariableDecl>,
    var typeExpr: TypeExpr,
    var implementedConcepts: MutableList<Name>,
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

    for (decl in decls) {
        visitor.visit(decl)
    }
}
