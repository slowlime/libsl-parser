package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.FullName
import org.jetbrains.research.libsl.ast.FunctionBody
import org.jetbrains.research.libsl.ast.FunctionLike
import org.jetbrains.research.libsl.ast.FunctionParam
import org.jetbrains.research.libsl.ast.Generic
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.TypeConstraint
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class FunctionDecl(
    override val location: Location?,
    override val annotations: MutableList<LibSLAnnotation>,
    val isStatic: Boolean,
    val extensionFor: FullName?,
    val isMethod: Boolean,
    override val name: Name,
    val generics: MutableList<Generic>,
    override val params: MutableList<FunctionParam>,
    override val returnType: TypeExpr?,
    val typeConstraints: MutableList<TypeConstraint>,
    override val body: FunctionBody?,
) : FunctionLike, GlobalDecl, StructMemberDecl, AutomatonMemberDecl
