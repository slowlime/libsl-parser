package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.contract.Contract
import org.jetbrains.research.libsl.ast.stmt.Stmt
import org.jetbrains.research.libsl.resolve.scope.MutableScope

data class FunctionBody(
    var contracts: MutableList<Contract>,
    var stmts: MutableList<Stmt>
) {
    lateinit var scope: MutableScope
}

fun FunctionBody.walk(visitor: Visitor) {
    for (contract in contracts) {
        visitor.visit(contract)
    }

    for (stmt in stmts) {
        visitor.visit(stmt)
    }
}
