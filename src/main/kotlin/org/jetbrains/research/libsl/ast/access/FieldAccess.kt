package org.jetbrains.research.libsl.ast.access

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.location.Location

data class FieldAccess(
    override var location: Location?,
    var base: Access,
    var field: Name,
) : Access
