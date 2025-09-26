package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.location.Location

sealed interface FloatLit : PrimitiveLit {
    data class F32(override var location: Location?, var value: Float) : FloatLit
    data class F64(override var location: Location?, var value: Double) : FloatLit

    companion object {
        fun of(location: Location?, value: Float): F32 = F32(location, value)
        fun of(location: Location?, value: Double): F64 = F64(location, value)
    }
}
