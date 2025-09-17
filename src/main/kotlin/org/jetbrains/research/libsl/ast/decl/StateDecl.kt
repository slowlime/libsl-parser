package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.location.Location

data class StateDecl(
    override val location: Location?,
    val kind: Kind,
    val name: Name,
) : AutomatonMemberDecl {
    enum class Kind {
        Initial,
        Regular,
        Final,
    }
}
