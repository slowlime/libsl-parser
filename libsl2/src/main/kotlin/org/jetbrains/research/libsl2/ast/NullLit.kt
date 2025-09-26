package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.location.Location

data class NullLit(override var location: Location?) : PrimitiveLit
