package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

sealed interface IntLit : PrimitiveLit {
    data class I8(override val location: Location?, val value: Byte) : IntLit
    data class U8(override val location: Location?, val value: Byte) : IntLit
    data class I16(override val location: Location?, val value: Short) : IntLit
    data class U16(override val location: Location?, val value: Short) : IntLit
    data class I32(override val location: Location?, val value: Int) : IntLit
    data class U32(override val location: Location?, val value: Int) : IntLit
    data class I64(override val location: Location?, val value: Long) : IntLit
    data class U64(override val location: Location?, val value: Long) : IntLit
}
