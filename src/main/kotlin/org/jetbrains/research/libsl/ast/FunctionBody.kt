package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.contract.Contract
import org.jetbrains.research.libsl.ast.stmt.Stmt

data class FunctionBody(
    val contracts: MutableList<Contract>,
    val stmts: MutableList<Stmt>
)
