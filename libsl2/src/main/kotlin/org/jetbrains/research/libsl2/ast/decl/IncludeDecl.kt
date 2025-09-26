package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.location.Location

data class IncludeDecl(
    override var location: Location?,
    var path: String,
) : GlobalDecl
