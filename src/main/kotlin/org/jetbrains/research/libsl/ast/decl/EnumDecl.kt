package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.IntLit
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.location.Location

data class EnumDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var typeName: QualifiedTypeName,
    var variants: MutableList<Variant>
) : GlobalDecl, Annotatable {
    data class Variant(var name: Name, var value: IntLit)
}
