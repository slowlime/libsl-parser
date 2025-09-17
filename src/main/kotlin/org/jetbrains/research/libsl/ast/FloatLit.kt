package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

sealed interface FloatLit : PrimitiveLit {
    data class F32(override val location: Location?, val value: Float) : FloatLit
    data class F64(override val location: Location?, val value: Double) : FloatLit
}
