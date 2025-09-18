package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.decl.GlobalDecl
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider

data class Module(
    override var location: Location?,
    var header: Header?,
    var decls: List<GlobalDecl>,
) : LocationProvider
