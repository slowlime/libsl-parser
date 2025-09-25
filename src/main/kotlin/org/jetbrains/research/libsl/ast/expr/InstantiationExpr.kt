package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.FullName
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.decl.AutomatonDecl
import org.jetbrains.research.libsl.ast.type.TypeArg
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def

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

    lateinit var resolved: Def<AutomatonDecl>
}

fun InstantiationExpr.walk(visitor: Visitor) {
    typeArgs?.forEach { visitor.visit(it) }

    for (arg in args) {
        arg.walk(visitor)
    }
}

fun InstantiationExpr.Arg.walk(visitor: Visitor) {
    when (this) {
        is InstantiationExpr.Arg.State -> visitor.visit(value)
        is InstantiationExpr.Arg.Var -> visitor.visit(value)
    }
}
