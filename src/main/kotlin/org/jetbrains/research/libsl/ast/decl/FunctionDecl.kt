package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.Annotation
import org.jetbrains.research.libsl.ast.FunctionParam
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class FunctionDecl(
    override val location: Location?,
    override val annotations: MutableList<Annotation>,
    val isStatic: Boolean,
    val extensionFor: FullName?,
    val isMethod: Boolean,
    val generics: MutableList<Generic>,
    val params: MutableList<FunctionParam>,
    val returnType: TypeExpr?,
    val typeConstraints: MutableList<TypeConstraint>,
    val body: FunctionBody?,
) : GlobalDecl, StructMemberDecl, AutomatonMemberDecl, Annotatable
