package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.decl.ActionDecl
import org.jetbrains.research.libsl.ast.type.TypeArg
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def

data class ActionCallExpr(
    override var location: Location?,
    var name: Name,
    var typeArgs: MutableList<TypeArg>?,
    var args: MutableList<Expr>,
) : Expr {
    lateinit var resolved: Def<ActionDecl>
}

fun ActionCallExpr.walk(visitor: Visitor) {
    typeArgs?.forEach { visitor.visit(it) }

    for (arg in args) {
        visitor.visit(arg)
    }
}
