package org.jetbrains.research.libsl2.ast.predicate

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.decl.VariableDecl

data class VariableDeclPredicate(
    var decl: VariableDecl
) : Predicate {
    override var location by decl::location
}

fun VariableDeclPredicate.walk(visitor: Visitor) {
    visitor.visit(decl)
}
