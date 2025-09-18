package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.FullName
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.type.TypeArg
import org.jetbrains.research.libsl.location.Location

data class InstantiationExpr(
    override var location: Location?,
    var name: FullName,
    var typeArgs: MutableList<TypeArg>?,
    var args: MutableList<Arg>,
) : Expr {
    sealed interface Arg {
        var value: Expr

        data class State(override var value: Expr) : Arg
        data class Var(var name: Name, override var value: Expr) : Arg
    }
}
