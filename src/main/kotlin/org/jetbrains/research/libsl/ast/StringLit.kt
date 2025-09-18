package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

data class StringLit(override var location: Location?, var value: String) : PrimitiveLit
