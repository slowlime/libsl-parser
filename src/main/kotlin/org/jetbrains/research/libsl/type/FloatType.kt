package org.jetbrains.research.libsl.type

class FloatType(val width: Width) : Type {
    enum class Width {
        F32,
        F64,
    }
}
