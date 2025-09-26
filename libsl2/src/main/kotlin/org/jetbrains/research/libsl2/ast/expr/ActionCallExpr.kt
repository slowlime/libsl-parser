package org.jetbrains.research.libsl2.ast.expr

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.decl.ActionDecl
import org.jetbrains.research.libsl2.ast.type.TypeArg
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Def

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
