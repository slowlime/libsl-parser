package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotation
import org.jetbrains.research.libsl.ast.FunctionBody
import org.jetbrains.research.libsl.ast.FunctionLike
import org.jetbrains.research.libsl.ast.FunctionParam
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class ConstructorDecl(
    override val location: Location?,
    override val annotations: MutableList<Annotation>,
    val isMethod: Boolean,
    override val name: Name,
    override val params: MutableList<FunctionParam>,
    override val returnType: TypeExpr?,
    override val body: FunctionBody?,
) : FunctionLike, AutomatonMemberDecl
