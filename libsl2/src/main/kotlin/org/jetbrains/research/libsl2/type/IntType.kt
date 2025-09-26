package org.jetbrains.research.libsl2.type

class IntType(val width: Width, val signed: Boolean) : Type {
    enum class Width {
        I8,
        I16,
        I32,
        I64,
    }
}
