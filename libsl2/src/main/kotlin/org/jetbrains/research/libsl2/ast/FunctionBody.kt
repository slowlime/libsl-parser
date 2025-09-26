package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.ast.contract.Contract
import org.jetbrains.research.libsl2.ast.stmt.Stmt
import org.jetbrains.research.libsl2.resolve.scope.MutableScope

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
