package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.LocationProvider

interface FunctionLike : LocationProvider, Annotatable {
    val name: Name
    val params: MutableList<FunctionParam>
    val returnType: TypeExpr?
    val body: FunctionBody?
}
