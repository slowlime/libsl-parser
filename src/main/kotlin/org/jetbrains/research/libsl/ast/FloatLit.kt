package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

sealed interface FloatLit : PrimitiveLit {
    data class F32(override var location: Location?, var value: Float) : FloatLit
    data class F64(override var location: Location?, var value: Double) : FloatLit
}
