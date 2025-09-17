package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

data class CharLit(override val location: Location?, val value: Int) : PrimitiveLit
