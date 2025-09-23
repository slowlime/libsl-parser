package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.Generic
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.TypeConstraint
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity

class ActionDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var name: Name,
    var generics: MutableList<Generic>,
    var params: MutableList<Param>,
    var returnType: TypeExpr?,
    var typeConstraints: MutableList<TypeConstraint>,
) : GlobalDecl, Annotatable, Entity<ActionDecl> {
    data class Param(
        override var annotations: MutableList<LibSLAnnotation>,
        var name: Name,
        var typeExpr: TypeExpr,
    ) : Annotatable

    override lateinit var primaryDef: Def.Primary<ActionDecl>
}
