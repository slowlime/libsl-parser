package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.FunctionBody
import org.jetbrains.research.libsl.ast.FunctionParam
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.LocationProvider

sealed interface FunctionLikeDecl : LocationProvider, Annotatable {
    var name: Name
    var params: MutableList<FunctionParam>
    var returnType: TypeExpr?
    var body: FunctionBody?
}
