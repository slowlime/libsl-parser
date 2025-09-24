package org.jetbrains.research.libsl.ast.stmt

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.LocationProvider

sealed interface Stmt : LocationProvider

fun Stmt.walk(visitor: Visitor) {
    when (this) {
        is AssignStmt -> visitor.visit(this)
        is ExprStmt -> visitor.visit(this)
        is IfStmt -> visitor.visit(this)
        is VariableDeclStmt -> visitor.visit(this)
    }
}
