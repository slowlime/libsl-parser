package org.jetbrains.research.libsl2.ast.access

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Binding
import org.jetbrains.research.libsl2.resolve.Def

data class NameAccess(var name: Name) : Access {
    override var location: Location? by name::location

    var resolved: Def<Binding>? = null
}
