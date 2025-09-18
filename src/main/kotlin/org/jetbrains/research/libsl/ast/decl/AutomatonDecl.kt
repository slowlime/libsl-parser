package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class AutomatonDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var isConcept: Boolean,
    var name: QualifiedTypeName,
    var constructorVariables: MutableList<VariableDecl>,
    var typeExpr: TypeExpr,
    var implementedConcepts: MutableList<Name>,
    var decls: MutableList<AutomatonMemberDecl>
) : GlobalDecl, Annotatable
