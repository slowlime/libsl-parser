package org.jetbrains.research.libsl2.load

import org.jetbrains.research.libsl2.LibSLParser
import org.jetbrains.research.libsl2.ast.access.FieldAccess
import org.jetbrains.research.libsl2.ast.access.IndexAccess
import org.jetbrains.research.libsl2.ast.access.NameAccess

internal class AccessProcessor(private val loader: ModuleLoader) {
    fun process(ctx: LibSLParser.AccessNameContext): NameAccess = NameAccess(loader.processName(ctx.name))

    fun process(ctx: LibSLParser.AccessFieldContext): FieldAccess = FieldAccess(
        loader.locationOf(ctx),
        loader.processAccess(ctx.base),
        loader.processName(ctx.field),
    )

    fun process(ctx: LibSLParser.AccessIndexContext): IndexAccess = IndexAccess(
        loader.locationOf(ctx),
        loader.processAccess(ctx.base),
        loader.processExpr(ctx.index),
    )
}
