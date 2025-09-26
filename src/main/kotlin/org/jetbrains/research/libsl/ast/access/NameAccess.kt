package org.jetbrains.research.libsl.ast.access

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Binding
import org.jetbrains.research.libsl.resolve.Def

data class NameAccess(var name: Name) : Access {
    override var location: Location? by name::location

    var resolved: Def<Binding>? = null
}
