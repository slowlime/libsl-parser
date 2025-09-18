package org.jetbrains.research.libsl.ast.stmt

import org.jetbrains.research.libsl.ast.decl.VariableDecl

data class VariableDeclStmt(
    var decl: VariableDecl
) : Stmt {
    override var location by decl::location
}
