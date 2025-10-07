package org.jetbrains.research.libsl2.ast.access

import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.decl.AutomatonDecl
import org.jetbrains.research.libsl2.ast.type.TypeArg
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Def

data class AutomatonFieldAccess(
    override var location: Location?,
    var name: Name,
    var typeArgs: MutableList<TypeArg>?,
    var inner: Access,
    var field: Name,
) : Access {
    lateinit var resolvedAutomaton: Def<AutomatonDecl>
}

fun AutomatonFieldAccess.walk(visitor: Visitor) {
    typeArgs?.forEach { visitor.visit(it) }
    visitor.visit(inner)
}

