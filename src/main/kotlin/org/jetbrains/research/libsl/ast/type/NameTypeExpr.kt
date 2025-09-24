package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.ast.FullName
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.type.Type

data class NameTypeExpr(
    override var location: Location?,
    var typeName: FullName,
    var typeArgs: MutableList<TypeArg>?,
) : TypeExpr {
    lateinit var resolvedTypeName: Def<Type>
}

fun NameTypeExpr.walk(visitor: Visitor) {
    typeArgs?.forEach { visitor.visit(it) }
}
