package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.Module
import org.jetbrains.research.libsl2.location.Location

data class ImportDecl(
    override var location: Location?,
    var path: String,
) : GlobalDecl {
    lateinit var importedModule: Module
}
