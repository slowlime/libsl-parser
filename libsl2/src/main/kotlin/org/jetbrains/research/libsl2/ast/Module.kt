package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.ast.decl.GlobalDecl
import org.jetbrains.research.libsl2.ast.decl.ImportDecl
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.location.LocationProvider
import org.jetbrains.research.libsl2.resolve.scope.ModuleScope

data class Module(
    override var location: Location?,
    var header: Header?,
    var decls: MutableList<GlobalDecl>,
) : LocationProvider {
    // initialized during name resolution
    val scope = ModuleScope(this)
    val imports: MutableList<ImportDecl> = mutableListOf()
    val importedBy: MutableList<Pair<Module, ImportDecl>> = mutableListOf()
}

fun Module.walk(visitor: Visitor) {
    for (decl in decls) {
        visitor.visit(decl)
    }
}
