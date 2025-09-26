package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.Annotatable
import org.jetbrains.research.libsl2.ast.LibSLAnnotation
import org.jetbrains.research.libsl2.ast.QualifiedTypeName
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity
import org.jetbrains.research.libsl2.resolve.scope.MutableScope
import org.jetbrains.research.libsl2.type.TypeConstructor

data class TypeAliasDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var typeName: QualifiedTypeName,
    var typeExpr: TypeExpr,
) : GlobalDecl, Annotatable, Entity<TypeConstructor> {
    override lateinit var primaryDef: Def.Primary<TypeConstructor>
    lateinit var scope: MutableScope
}

fun TypeAliasDecl.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    visitor.visit(typeExpr)
}
