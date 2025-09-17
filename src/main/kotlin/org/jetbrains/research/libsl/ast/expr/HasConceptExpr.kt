package org.jetbrains.research.libsl.ast.expr

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.location.Location

data class HasConceptExpr(
    override val location: Location?,
    val lhs: Access,
    val concept: Name,
) : Expr
