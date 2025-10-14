package org.jetbrains.research.libsl2.ast.predicate

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.LocationProvider

sealed interface Predicate : LocationProvider

fun Predicate.walk(visitor: Visitor) {
    when (this) {
        is BlockPredicate -> visitor.visit(this)
        is ExprPredicate -> visitor.visit(this)
        is IfPredicate -> visitor.visit(this)
        is NamedPredicate -> visitor.visit(this)
        is VariableDeclPredicate -> visitor.visit(this)
    }
}
