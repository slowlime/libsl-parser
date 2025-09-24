package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.FunctionSignature
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.walk
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def

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
