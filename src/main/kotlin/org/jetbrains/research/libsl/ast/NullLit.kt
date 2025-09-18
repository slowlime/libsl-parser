package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

data class NullLit(override var location: Location?) : PrimitiveLit
