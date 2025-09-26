package org.jetbrains.research.libsl2.type

class FloatType(val width: Width) : Type {
    enum class Width {
        F32,
        F64,
    }
}
