package org.jetbrains.research.libsl2.load

import org.jetbrains.research.libsl2.LibSLParser
import org.jetbrains.research.libsl2.ast.type.IntersectionTypeExpr
import org.jetbrains.research.libsl2.ast.type.NameTypeExpr
import org.jetbrains.research.libsl2.ast.type.PointerTypeExpr
import org.jetbrains.research.libsl2.ast.type.PrimitiveLitTypeExpr
import org.jetbrains.research.libsl2.ast.type.UnionTypeExpr

internal class TypeExprProcessor(private val loader: ModuleLoader) {
    fun process(ctx: LibSLParser.TypeExprPrimitiveLitContext): PrimitiveLitTypeExpr = PrimitiveLitTypeExpr(
        loader.processPrimitiveLit(ctx.lit)
    )

    fun process(ctx: LibSLParser.NameTypeExprContext): NameTypeExpr = NameTypeExpr(
        loader.locationOf(ctx),
        loader.processFullName(ctx.typeName),
        ctx.typeArgs?.list?.typeArgs.mapToMutable(loader::processTypeArg),
    )

    fun process(ctx: LibSLParser.PointerTypeExprContext): PointerTypeExpr = PointerTypeExpr(
        loader.locationOf(ctx),
        loader.processTypeExpr(ctx.base),
    )

    fun process(ctx: LibSLParser.TypeExprIntersectionContext): IntersectionTypeExpr = IntersectionTypeExpr(
        loader.locationOf(ctx),
        loader.processTypeExpr(ctx.lhs),
        loader.processTypeExpr(ctx.rhs),
    )

    fun process(ctx: LibSLParser.TypeExprUnionContext): UnionTypeExpr = UnionTypeExpr(
        loader.locationOf(ctx),
        loader.processTypeExpr(ctx.lhs),
        loader.processTypeExpr(ctx.rhs),
    )
}
