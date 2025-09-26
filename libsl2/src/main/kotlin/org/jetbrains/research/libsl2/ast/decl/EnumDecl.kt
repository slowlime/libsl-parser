package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.Annotatable
import org.jetbrains.research.libsl2.ast.IntLit
import org.jetbrains.research.libsl2.ast.LibSLAnnotation
import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.QualifiedTypeName
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Binding
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity
import org.jetbrains.research.libsl2.resolve.scope.MutableScope
import org.jetbrains.research.libsl2.type.TypeConstructor

data class EnumDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var typeName: QualifiedTypeName,
    var variants: MutableList<Variant>
) : GlobalDecl, Annotatable, Entity<TypeConstructor> {
    data class Variant(var name: Name, var value: IntLit) : Entity<Binding> {
        override lateinit var primaryDef: Def.Primary<Binding>
    }

    override lateinit var primaryDef: Def.Primary<TypeConstructor>
    lateinit var scope: MutableScope
}

fun EnumDecl.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }
}
