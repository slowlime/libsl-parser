package org.jetbrains.research.libsl.load

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.ast.contract.AssignsContract
import org.jetbrains.research.libsl.ast.contract.EnsuresContract
import org.jetbrains.research.libsl.ast.contract.RequiresContract

internal class ContractProcessor(private val loader: ModuleLoader) {
    fun process(ctx: LibSLParser.RequiresContractContext): RequiresContract = RequiresContract(
        loader.locationOf(ctx),
        ctx.name?.let(loader::processName),
        loader.processExpr(ctx.spec),
    )

    fun process(ctx: LibSLParser.EnsuresContractContext): EnsuresContract = EnsuresContract(
        loader.locationOf(ctx),
        ctx.name?.let(loader::processName),
        loader.processExpr(ctx.spec),
    )

    fun process(ctx: LibSLParser.AssignsContractContext): AssignsContract = AssignsContract(
        loader.locationOf(ctx),
        ctx.name?.let(loader::processName),
        loader.processExpr(ctx.spec),
    )
}
