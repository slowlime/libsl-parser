package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.decl.GlobalDecl
import org.jetbrains.research.libsl.ast.decl.ImportDecl
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider
import org.jetbrains.research.libsl.resolve.scope.ModuleScope
import org.jetbrains.research.libsl.resolve.scope.Scoped

data class Module(
    override var location: Location?,
    var header: Header?,
    var decls: MutableList<GlobalDecl>,
) : LocationProvider, Scoped {
    // initialized during name resolution
    override val scope = ModuleScope(this)
    val imports: MutableList<ImportDecl> = mutableListOf()
    val importedBy: MutableList<Pair<Module, ImportDecl>> = mutableListOf()
}

fun Module.walk(visitor: Visitor) {
    for (decl in decls) {
        visitor.visit(decl)
    }
}
