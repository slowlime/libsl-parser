package org.jetbrains.research.libsl2.load

import org.jetbrains.research.libsl2.LibSLParser
import org.jetbrains.research.libsl2.ast.contract.AssignsContract
import org.jetbrains.research.libsl2.ast.contract.EnsuresContract
import org.jetbrains.research.libsl2.ast.contract.RequiresContract

internal class ContractProcessor(private val loader: ModuleLoader) {
    fun process(ctx: LibSLParser.RequiresContractContext): RequiresContract = RequiresContract(
        loader.locationOf(ctx),
        ctx.name?.let(loader::processName),
        loader.processPredicate(ctx.spec),
    )

    fun process(ctx: LibSLParser.EnsuresContractContext): EnsuresContract = EnsuresContract(
        loader.locationOf(ctx),
        ctx.name?.let(loader::processName),
        loader.processPredicate(ctx.spec),
    )

    fun process(ctx: LibSLParser.AssignsContractContext): AssignsContract = AssignsContract(
        loader.locationOf(ctx),
        ctx.name?.let(loader::processName),
        loader.processExpr(ctx.spec),
    )
}
