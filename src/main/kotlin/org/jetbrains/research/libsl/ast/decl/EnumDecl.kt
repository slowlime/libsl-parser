package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.Annotation
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.location.Location

data class EnumDecl(
    override val location: Location?,
    override val annotations: MutableList<Annotation>,
    val typeName: QualifiedTypeName,
    val variants: MutableList<Variant>
) : GlobalDecl, Annotatable {
    data class Variant(val name: Name, val value: IntLit)
}
