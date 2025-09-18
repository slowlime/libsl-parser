package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.LocationProvider

data class Name(
    override var location: Location?,
    var name: String,
) : LocationProvider
