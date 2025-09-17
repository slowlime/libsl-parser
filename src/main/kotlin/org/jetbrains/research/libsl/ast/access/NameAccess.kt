package org.jetbrains.research.libsl.ast.access

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.location.Location

data class NameAccess(val name: Name) : Access {
    override val location: Location? by name::location
}
