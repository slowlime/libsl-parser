package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.location.Location

data class BoolLit(override var location: Location?, var value: Boolean) :
    PrimitiveLit
