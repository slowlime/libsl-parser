package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

sealed interface IntLit : PrimitiveLit {
    data class I8(override var location: Location?, var value: Byte) : IntLit
    data class U8(override var location: Location?, var value: Byte) : IntLit
    data class I16(override var location: Location?, var value: Short) : IntLit
    data class U16(override var location: Location?, var value: Short) : IntLit
    data class I32(override var location: Location?, var value: Int) : IntLit
    data class U32(override var location: Location?, var value: Int) : IntLit
    data class I64(override var location: Location?, var value: Long) : IntLit
    data class U64(override var location: Location?, var value: Long) : IntLit
}
