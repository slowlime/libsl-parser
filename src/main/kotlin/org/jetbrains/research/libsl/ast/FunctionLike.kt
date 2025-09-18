package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.LocationProvider

interface FunctionLike : LocationProvider, Annotatable {
    var name: Name
    var params: MutableList<FunctionParam>
    var returnType: TypeExpr?
    var body: FunctionBody?
}
