package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.location.Location

data class StateDecl(
    override var location: Location?,
    var kind: Kind,
    var name: Name,
) : AutomatonMemberDecl {
    enum class Kind {
        Initial,
        Regular,
        Final,
    }
}
