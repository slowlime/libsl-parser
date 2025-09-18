package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class VariableDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var mutable: Boolean,
    var typeExpr: TypeExpr,
    var init: Expr?,
) : GlobalDecl, StructMemberDecl, AutomatonMemberDecl, Annotatable
