package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.location.Location

data class IncludeDecl(
    override var location: Location?,
    var path: String,
) : GlobalDecl
