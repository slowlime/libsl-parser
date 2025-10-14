package org.jetbrains.research.libsl2.load

import org.jetbrains.research.libsl2.LibSLParser
import org.jetbrains.research.libsl2.ast.expr.AccessExpr
import org.jetbrains.research.libsl2.ast.expr.ActionCallExpr
import org.jetbrains.research.libsl2.ast.expr.ArrayLitExpr
import org.jetbrains.research.libsl2.ast.expr.BinaryExpr
import org.jetbrains.research.libsl2.ast.expr.CastExpr
import org.jetbrains.research.libsl2.ast.expr.Expr
import org.jetbrains.research.libsl2.ast.expr.HasConceptExpr
import org.jetbrains.research.libsl2.ast.expr.InstantiationExpr
import org.jetbrains.research.libsl2.ast.expr.PrevExpr
import org.jetbrains.research.libsl2.ast.expr.PrimitiveLitExpr
import org.jetbrains.research.libsl2.ast.expr.ProcCallExpr
import org.jetbrains.research.libsl2.ast.expr.SetLitExpr
import org.jetbrains.research.libsl2.ast.expr.TypeCmpExpr
import org.jetbrains.research.libsl2.ast.expr.UnaryExpr

internal class ExprProcessor(private val loader: ModuleLoader) {
    fun process(ctx: LibSLParser.AtomicExprPrimitiveLitContext): PrimitiveLitExpr = PrimitiveLitExpr(
        loader.processPrimitiveLit(ctx.lit),
    )

    fun process(ctx: LibSLParser.ExprPrimitiveLitContext): PrimitiveLitExpr = PrimitiveLitExpr(
        loader.processPrimitiveLit(ctx.lit),
    )

    fun process(ctx: LibSLParser.AtomicExprSignedNumLitContext): PrimitiveLitExpr = PrimitiveLitExpr(
        loader.processSignedNumLit(ctx.signedNumLit()),
    )

    fun process(ctx: LibSLParser.ArrayLitExprContext): ArrayLitExpr = ArrayLitExpr(
        loader.locationOf(ctx),
        ctx.elems?.exprs.mapToMutable(loader::processExpr),
    )

    fun process(ctx: LibSLParser.SetLitExprContext): SetLitExpr = SetLitExpr(
        loader.locationOf(ctx),
        ctx.elems?.exprs.mapToMutable(loader::processExpr),
    )

    fun process(ctx: LibSLParser.ExprPrevContext): PrevExpr = PrevExpr(
        loader.locationOf(ctx),
        loader.processAccess(ctx.base),
    )

    fun process(ctx: LibSLParser.ProcCallExprContext): ProcCallExpr = ProcCallExpr(
        loader.locationOf(ctx),
        loader.processAccess(ctx.callee),
        ctx.typeArgs?.list?.typeArgs.mapToMutable(loader::processTypeArg),
        ctx.args?.exprs.mapToMutable(loader::processExpr),
    )

    fun process(ctx: LibSLParser.ActionCallExprContext): ActionCallExpr = ActionCallExpr(
        loader.locationOf(ctx),
        loader.processName(ctx.name),
        ctx.typeArgs?.list?.typeArgs.mapToMutable(loader::processTypeArg),
        ctx.args?.exprs.mapToMutable(loader::processExpr),
    )

    fun process(ctx: LibSLParser.InstantiationExprContext): InstantiationExpr = InstantiationExpr(
        loader.locationOf(ctx),
        loader.processFullName(ctx.name),
        ctx.typeArgs?.list?.typeArgs.mapToMutable(loader::processTypeArg),
        ctx.args?.args.mapToMutable { arg ->
            when (arg) {
                is LibSLParser.ConstructorArgStateContext -> InstantiationExpr.Arg.State(
                    loader.processName(arg.state),
                )

                is LibSLParser.ConstructorArgVarContext -> InstantiationExpr.Arg.Var(
                    loader.processName(arg.name),
                    loader.processExpr(arg.value),
                )

                else -> error("unrecognized constructor arg $ctx")
            }
        },
    )

    fun process(ctx: LibSLParser.AtomicExprAccessContext): AccessExpr = AccessExpr(
        loader.processAccess(ctx.access()),
    )

    fun process(ctx: LibSLParser.ExprAccessContext): AccessExpr = AccessExpr(
        loader.processAccess(ctx.access()),
    )

    fun process(ctx: LibSLParser.ExprUnaryContext): Expr {
        val op = when (ctx.op) {
            is LibSLParser.UnOpNegContext -> UnaryExpr.Op.Neg
            is LibSLParser.UnOpPlusContext -> UnaryExpr.Op.Plus
            is LibSLParser.UnOpBitNotContext -> UnaryExpr.Op.BitNot
            is LibSLParser.UnOpNotContext -> UnaryExpr.Op.Not
            else -> error("unrecognized unary op ${ctx.op}")
        }

        val sign = when (ctx.op) {
            is LibSLParser.UnOpNegContext -> -1
            is LibSLParser.UnOpPlusContext -> 1
            else -> 0
        }

        val rhs = ctx.rhs

        // treat expressions like -2147483648 (the smallest Int) specially,
        // because 2147483648 on its own is outside the range of Int.
        if (sign != 0 && rhs is LibSLParser.ExprPrimitiveLitContext) {
            val primitiveLitExpr = when (val lit = rhs.lit) {
                is LibSLParser.PrimitiveLitIntContext -> PrimitiveLitExpr(
                    loader.processIntLit(
                        sign,
                        lit.IntegerLit().symbol,
                    ),
                )

                is LibSLParser.PrimitiveLitFloatContext -> PrimitiveLitExpr(
                    loader.processFloatLit(
                        sign,
                        lit.FloatLit().symbol,
                    ),
                )

                else -> null
            }

            if (primitiveLitExpr != null) {
                primitiveLitExpr.lit.location = loader.locationOf(ctx)

                return primitiveLitExpr
            }
        }

        return UnaryExpr(
            loader.locationOf(ctx),
            op,
            loader.processExpr(rhs),
        )
    }

    fun process(ctx: LibSLParser.ExprHasConceptContext): HasConceptExpr = HasConceptExpr(
        loader.locationOf(ctx),
        loader.processAccess(ctx.lhs),
        loader.processName(ctx.concept),
    )

    fun process(ctx: LibSLParser.ExprTypeComparisonContext): TypeCmpExpr = TypeCmpExpr(
        loader.locationOf(ctx),
        loader.processExpr(ctx.lhs),
        loader.processTypeExpr(ctx.type),
    )

    fun process(ctx: LibSLParser.ExprCastContext): CastExpr = CastExpr(
        loader.locationOf(ctx),
        loader.processExpr(ctx.lhs),
        loader.processTypeExpr(ctx.type),
    )

    fun process(ctx: LibSLParser.ExprMultiplicativeContext): BinaryExpr = BinaryExpr(
        loader.locationOf(ctx),
        loader.processExpr(ctx.lhs),
        when (ctx.op) {
            is LibSLParser.BinOpMulContext -> BinaryExpr.Op.Mul
            is LibSLParser.BinOpDivContext -> BinaryExpr.Op.Div
            is LibSLParser.BinOpModContext -> BinaryExpr.Op.Mod
            else -> error("unrecognized multiplicative binary op $ctx")
        },
        loader.processExpr(ctx.rhs),
    )

    fun process(ctx: LibSLParser.ExprAdditiveContext): BinaryExpr = BinaryExpr(
        loader.locationOf(ctx),
        loader.processExpr(ctx.lhs),
        when (ctx.op) {
            is LibSLParser.BinOpAddContext -> BinaryExpr.Op.Add
            is LibSLParser.BinOpSubContext -> BinaryExpr.Op.Sub
            else -> error("unrecognized additive binary op $ctx")
        },
        loader.processExpr(ctx.rhs),
    )

    fun process(ctx: LibSLParser.ExprShiftContext): BinaryExpr = BinaryExpr(
        loader.locationOf(ctx),
        loader.processExpr(ctx.lhs),
        when (ctx.op) {
            is LibSLParser.BinOpLogicalLeftContext -> BinaryExpr.Op.Shl
            is LibSLParser.BinOpLogicalRightContext -> BinaryExpr.Op.Shr
            is LibSLParser.BinOpArithmeticLeftContext -> BinaryExpr.Op.Sal
            is LibSLParser.BinOpArithmeticRightContext -> BinaryExpr.Op.Sar
            else -> error("unrecognized binary shift op $ctx")
        },
        loader.processExpr(ctx.rhs),
    )

    fun process(ctx: LibSLParser.ExprBitAndContext): BinaryExpr = BinaryExpr(
        loader.locationOf(ctx),
        loader.processExpr(ctx.lhs),
        BinaryExpr.Op.BitAnd,
        loader.processExpr(ctx.rhs),
    )

    fun process(ctx: LibSLParser.ExprBitXorContext): BinaryExpr = BinaryExpr(
        loader.locationOf(ctx),
        loader.processExpr(ctx.lhs),
        BinaryExpr.Op.BitXor,
        loader.processExpr(ctx.rhs),
    )

    fun process(ctx: LibSLParser.ExprBitOrContext): BinaryExpr = BinaryExpr(
        loader.locationOf(ctx),
        loader.processExpr(ctx.lhs),
        BinaryExpr.Op.BitOr,
        loader.processExpr(ctx.rhs),
    )

    fun process(ctx: LibSLParser.ExprRelationalContext): BinaryExpr = BinaryExpr(
        loader.locationOf(ctx),
        loader.processExpr(ctx.lhs),
        when (ctx.op) {
            is LibSLParser.BinOpLessEqualsContext -> BinaryExpr.Op.Le
            is LibSLParser.BinOpGreaterEqualsContext -> BinaryExpr.Op.Ge
            is LibSLParser.BinOpLessContext -> BinaryExpr.Op.Lt
            is LibSLParser.BinOpGreaterContext -> BinaryExpr.Op.Gt
            is LibSLParser.BinOpEqualsContext -> BinaryExpr.Op.Eq
            is LibSLParser.BinOpNotEqualsContext -> BinaryExpr.Op.Ne
            is LibSLParser.BinOpInContext -> BinaryExpr.Op.In
            else -> error("unrecognized relational binary op $ctx")
        },
        loader.processExpr(ctx.rhs),
    )

    fun process(ctx: LibSLParser.ExprAndContext): BinaryExpr = BinaryExpr(
        loader.locationOf(ctx),
        loader.processExpr(ctx.lhs),
        BinaryExpr.Op.And,
        loader.processExpr(ctx.rhs),
    )

    fun process(ctx: LibSLParser.ExprOrContext): BinaryExpr = BinaryExpr(
        loader.locationOf(ctx),
        loader.processExpr(ctx.lhs),
        BinaryExpr.Op.Or,
        loader.processExpr(ctx.rhs),
    )
}
