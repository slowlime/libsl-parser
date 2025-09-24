package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.IntLit
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Binding
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity
import org.jetbrains.research.libsl.resolve.scope.MutableScope
import org.jetbrains.research.libsl.type.Type

data class EnumDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var typeName: QualifiedTypeName,
    var variants: MutableList<Variant>
) : GlobalDecl, Annotatable, Entity<Type> {
    data class Variant(var name: Name, var value: IntLit) : Entity<Binding> {
        override lateinit var primaryDef: Def.Primary<Binding>
    }

    override lateinit var primaryDef: Def.Primary<Type>
    lateinit var scope: MutableScope
}

fun EnumDecl.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }
}
