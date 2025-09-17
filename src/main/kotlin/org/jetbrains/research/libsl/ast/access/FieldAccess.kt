package org.jetbrains.research.libsl.ast.access

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.location.Location

data class FieldAccess(
    override val location: Location?,
    val base: Access,
    val field: Name,
) : Access
