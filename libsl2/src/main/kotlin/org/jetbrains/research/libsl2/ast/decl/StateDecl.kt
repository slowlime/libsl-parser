package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity

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
