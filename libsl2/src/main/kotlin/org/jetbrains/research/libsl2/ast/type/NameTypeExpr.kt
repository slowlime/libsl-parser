package org.jetbrains.research.libsl2.ast.type

import org.jetbrains.research.libsl2.ast.FullName
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.type.TypeConstructor

data class NameTypeExpr(
    override var location: Location?,
    var typeName: FullName,
    var typeArgs: MutableList<TypeArg>?,
) : TypeExpr {
    lateinit var resolvedTypeName: Def<TypeConstructor>
}

fun NameTypeExpr.walk(visitor: Visitor) {
    typeArgs?.forEach { visitor.visit(it) }
}
