package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity
import org.jetbrains.research.libsl.resolve.scope.MutableScope
import org.jetbrains.research.libsl.type.TypeConstructor

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
