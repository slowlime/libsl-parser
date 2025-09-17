package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

data class StringLit(override val location: Location?, val value: String) : PrimitiveLit
