package org.jetbrains.research.libsl.load

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.ast.stmt.AssignStmt
import org.jetbrains.research.libsl.ast.stmt.ExprStmt
import org.jetbrains.research.libsl.ast.stmt.IfStmt
import org.jetbrains.research.libsl.ast.stmt.Stmt
import org.jetbrains.research.libsl.ast.stmt.VariableDeclStmt

internal class StmtProcessor(private val loader: ModuleLoader) {
    fun process(ctx: LibSLParser.StmtVariableDeclContext): VariableDeclStmt = VariableDeclStmt(
        loader.processVariableDecl(ctx.variableDecl()),
    )

    fun process(ctx: LibSLParser.IfStmtContext): IfStmt = IfStmt(
        loader.locationOf(ctx),
        loader.processExpr(ctx.condition),
        processBlock(ctx.thenBranch),
        ctx.elseBranch?.let(::processBlock),
    )

    fun process(ctx: LibSLParser.AssignStmtContext): AssignStmt = AssignStmt(
        loader.locationOf(ctx),
        loader.processAccess(ctx.lhs),
        when (ctx.op) {
            is LibSLParser.OpAssignContext -> null
            is LibSLParser.OpAddAssignContext -> AssignStmt.InPlaceOp.Add
            is LibSLParser.OpSubAssignContext -> AssignStmt.InPlaceOp.Sub
            is LibSLParser.OpMulAssignContext -> AssignStmt.InPlaceOp.Mul
            is LibSLParser.OpDivAssignContext -> AssignStmt.InPlaceOp.Div
            is LibSLParser.OpModAssignContext -> AssignStmt.InPlaceOp.Mod
            is LibSLParser.OpBitAndAssignContext -> AssignStmt.InPlaceOp.BitAnd
            is LibSLParser.OpBitOrAssignContext -> AssignStmt.InPlaceOp.BitOr
            is LibSLParser.OpBitXorAssignContext -> AssignStmt.InPlaceOp.BitXor
            is LibSLParser.OpLShiftAssignContext -> AssignStmt.InPlaceOp.LShift
            is LibSLParser.OpRShiftAssignContext -> AssignStmt.InPlaceOp.RShift
            else -> error("unrecognized assign op ${ctx.op}")
        },
        loader.processExpr(ctx.rhs),
    )

    fun process(ctx: LibSLParser.StmtExprContext): ExprStmt = ExprStmt(
        loader.locationOf(ctx),
        loader.processExpr(ctx.inner),
    )

    private fun processBlock(ctx: LibSLParser.BlockContext): MutableList<Stmt> = when (ctx) {
        is LibSLParser.BlockLoneStmtContext -> mutableListOf(loader.processStmt(ctx.stmt()))
        is LibSLParser.BlockBracedContext -> ctx.stmts.mapToMutable(loader::processStmt)
        else -> error("unrecognized block $ctx")
    }
}
