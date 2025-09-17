package org.jetbrains.research.libsl.ast.contract

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.location.Location

data class AssignsContract(
    override val location: Location?,
    val name: Name?,
    val expr: Expr,
) : Contract
