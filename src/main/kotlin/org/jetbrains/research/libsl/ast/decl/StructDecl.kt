package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.Annotation
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.ast.TypeConstraint
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

data class StructDecl(
    override val location: Location?,
    override val annotations: MutableList<Annotation>,
    val typeName: QualifiedTypeName,
    val isType: TypeExpr?,
    val forTypes: MutableList<TypeExpr>,
    val typeConstraints: MutableList<TypeConstraint>,
    val decls: MutableList<StructMemberDecl>,
) : GlobalDecl, Annotatable
