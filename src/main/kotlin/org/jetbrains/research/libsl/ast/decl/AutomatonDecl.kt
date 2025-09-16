package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.Annotation
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class AutomatonDecl(
    override val location: Location?,
    override val annotations: MutableList<Annotation>,
    val isConcept: Boolean,
    val name: QualifiedTypeName,
    val constructorVariables: MutableList<VariableDecl>,
    val typeExpr: TypeExpr,
    val implementedConcepts: MutableList<Name>,
    val decls: MutableList<AutomatonMemberDecl>
) : GlobalDecl, Annotatable
