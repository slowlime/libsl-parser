package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

data class NullLit(override val location: Location?) : PrimitiveLit
