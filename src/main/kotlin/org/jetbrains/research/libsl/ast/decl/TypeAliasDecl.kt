package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.Annotation
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class TypeAliasDecl(
    override val location: Location?,
    override val annotations: MutableList<Annotation>,
    val typeName: QualifiedTypeName,
    val typeExpr: TypeExpr,
) : GlobalDecl, Annotatable
