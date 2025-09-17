package org.jetbrains.research.libsl.ast.stmt

import org.jetbrains.research.libsl.ast.decl.VariableDecl

data class VariableDeclStmt(
    val decl: VariableDecl
) : Stmt {
    override val location by decl::location
}
