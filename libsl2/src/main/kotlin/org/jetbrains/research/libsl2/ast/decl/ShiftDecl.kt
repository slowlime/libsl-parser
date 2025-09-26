package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.FunctionSignature
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.walk
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Def

data class ShiftDecl(
    override var location: Location?,
    var from: MutableList<Name>,
    var to: Name,
    var by: MutableList<FunctionSignature>,
) : AutomatonMemberDecl {
    lateinit var fromStates: MutableList<Def<StateDecl>>
    lateinit var toState: Def<StateDecl>
}

fun ShiftDecl.walk(visitor: Visitor) {
    for (sig in by) {
        sig.walk(visitor)
    }
}
