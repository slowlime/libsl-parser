package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity

data class StateDecl(
    override var location: Location?,
    var kind: Kind,
    var name: Name,
) : AutomatonMemberDecl, Entity<StateDecl> {
    enum class Kind {
        Initial,
        Regular,
        Final,
    }

    override lateinit var primaryDef: Def.Primary<StateDecl>
}
