package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

data class BoolLit(override var location: Location?, var value: Boolean) : PrimitiveLit
