package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

sealed interface IntLit : PrimitiveLit {
    data class I8(override var location: Location?, var value: Byte) : IntLit
    data class U8(override var location: Location?, var value: UByte) : IntLit
    data class I16(override var location: Location?, var value: Short) : IntLit
    data class U16(override var location: Location?, var value: UShort) : IntLit
    data class I32(override var location: Location?, var value: Int) : IntLit
    data class U32(override var location: Location?, var value: UInt) : IntLit
    data class I64(override var location: Location?, var value: Long) : IntLit
    data class U64(override var location: Location?, var value: ULong) : IntLit

    companion object {
        fun of(location: Location?, value: Byte): I8 = I8(location, value)
        fun of(location: Location?, value: UByte): U8 = U8(location, value)
        fun of(location: Location?, value: Short): I16 = I16(location, value)
        fun of(location: Location?, value: UShort): U16 = U16(location, value)
        fun of(location: Location?, value: Int): I32 = I32(location, value)
        fun of(location: Location?, value: UInt): U32 = U32(location, value)
        fun of(location: Location?, value: Long): I64 = I64(location, value)
        fun of(location: Location?, value: ULong): U64 = U64(location, value)
    }
}
