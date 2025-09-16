package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.location.Location

data class ImportDecl(
    override val location: Location?,
    val path: String,
) : Decl
