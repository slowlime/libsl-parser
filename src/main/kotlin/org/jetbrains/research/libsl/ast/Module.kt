package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.decl.GlobalDecl
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider

data class Module(
    override val location: Location?,
    val header: Header?,
    val decls: List<GlobalDecl>,
) : LocationProvider
