package org.jetbrains.research.libsl.ast.access

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.location.Location

data class NameAccess(var name: Name) : Access {
    override var location: Location? by name::location
}
