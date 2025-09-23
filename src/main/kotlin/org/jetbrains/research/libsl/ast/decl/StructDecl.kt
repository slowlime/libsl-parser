package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.ast.TypeConstraint
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity
import org.jetbrains.research.libsl.type.Type

data class StructDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var typeName: QualifiedTypeName,
    var isType: TypeExpr?,
    var forTypes: MutableList<TypeExpr>,
    var typeConstraints: MutableList<TypeConstraint>,
    var decls: MutableList<StructMemberDecl>,
) : GlobalDecl, Annotatable, Entity<Type> {
    override lateinit var primaryDef: Def.Primary<Type>
}
