package org.jetbrains.research.libsl2.load

import org.jetbrains.research.libsl2.LibSLParser
import org.jetbrains.research.libsl2.ast.predicate.BlockPredicate
import org.jetbrains.research.libsl2.ast.predicate.ExprPredicate
import org.jetbrains.research.libsl2.ast.predicate.IfPredicate
import org.jetbrains.research.libsl2.ast.predicate.NamedPredicate
import org.jetbrains.research.libsl2.ast.predicate.VariableDeclPredicate

internal class PredicateProcessor(private val loader: ModuleLoader) {
    fun process(ctx: LibSLParser.BlockPredicateContext): BlockPredicate = BlockPredicate(
        loader.locationOf(ctx),
        ctx.predicates.mapToMutable(loader::processPredicate),
    )

    fun process(ctx: LibSLParser.PredicateNamedContext): NamedPredicate = NamedPredicate(
        loader.locationOf(ctx),
        loader.processName(ctx.name),
        loader.processPredicate(ctx.predicate()),
    )

    fun process(ctx: LibSLParser.PredicateVariableDeclContext): VariableDeclPredicate = VariableDeclPredicate(
        loader.processVariableDecl(ctx.variableDecl()),
    )

    fun process(ctx: LibSLParser.IfPredicateContext): IfPredicate = IfPredicate(
        loader.locationOf(ctx),
        loader.processPredicate(ctx.condition),
        loader.processPredicate(ctx.thenBranch),
        ctx.elseBranch?.let(loader::processPredicate),
    )

    fun process(ctx: LibSLParser.PredicateExprContext): ExprPredicate = ExprPredicate(
        loader.locationOf(ctx),
        loader.processExpr(ctx.expr()),
    )

    fun process(ctx: LibSLParser.ContractPredicateExprContext): ExprPredicate = ExprPredicate(
        loader.locationOf(ctx),
        loader.processExpr(ctx.expr()),
    )

    fun process(ctx: LibSLParser.ExprPredicateExprContext): ExprPredicate = ExprPredicate(
        loader.locationOf(ctx),
        loader.processExpr(ctx.expr()),
    )
}
