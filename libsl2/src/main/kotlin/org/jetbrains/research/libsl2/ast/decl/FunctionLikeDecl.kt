package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.Annotatable
import org.jetbrains.research.libsl2.ast.FunctionBody
import org.jetbrains.research.libsl2.ast.FunctionParam
import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.location.LocationProvider

sealed interface FunctionLikeDecl : LocationProvider, Annotatable {
    var name: Name
    var params: MutableList<FunctionParam>
    var returnType: TypeExpr?
    var body: FunctionBody?
}
