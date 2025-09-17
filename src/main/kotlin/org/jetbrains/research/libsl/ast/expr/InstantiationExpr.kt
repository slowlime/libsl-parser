package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.FullName
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.type.TypeArg
import org.jetbrains.research.libsl.location.Location

data class InstantiationExpr(
    override val location: Location?,
    val name: FullName,
    val typeArgs: MutableList<TypeArg>?,
    val args: MutableList<Arg>,
) : Expr {
    sealed interface Arg {
        val value: Expr

        data class State(override val value: Expr) : Arg
        data class Var(val name: Name, override val value: Expr) : Arg
    }
}
