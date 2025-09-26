package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.ast.decl.AnnotationDecl
import org.jetbrains.research.libsl2.ast.expr.Expr
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.location.LocationProvider
import org.jetbrains.research.libsl2.resolve.Def
import kotlin.properties.Delegates

data class LibSLAnnotation(
    override var location: Location?,
    var name: Name,
    var args: MutableList<Arg>
) : LocationProvider {
    data class Arg(
        var name: Name?,
        var expr: Expr,
    ) {
        var paramIndex by Delegates.notNull<Int>()
    }

    lateinit var resolved: Def<AnnotationDecl>
}

fun LibSLAnnotation.walk(visitor: Visitor) {
    for (arg in args) {
        arg.walk(visitor)
    }
}

fun LibSLAnnotation.Arg.walk(visitor: Visitor) {
    visitor.visit(expr)
}
