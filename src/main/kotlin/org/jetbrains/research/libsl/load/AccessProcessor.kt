package org.jetbrains.research.libsl.load

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.ast.access.FieldAccess
import org.jetbrains.research.libsl.ast.access.IndexAccess
import org.jetbrains.research.libsl.ast.access.NameAccess

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
