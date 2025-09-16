package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.Annotation
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class VariableDecl(
    override val location: Location?,
    override val annotations: MutableList<Annotation>,
    val mutable: Boolean,
    val typeExpr: TypeExpr,
    val init: Expr?,
) : GlobalDecl, StructMemberDecl, AutomatonMemberDecl, Annotatable
