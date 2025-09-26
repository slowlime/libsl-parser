package org.jetbrains.research.libsl2.ast.stmt

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.decl.VariableDecl

data class VariableDeclStmt(
    var decl: VariableDecl
) : Stmt {
    override var location by decl::location
}

fun VariableDeclStmt.walk(visitor: Visitor) {
    visitor.visit(decl)
}
