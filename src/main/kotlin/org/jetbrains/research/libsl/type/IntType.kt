package org.jetbrains.research.libsl.type

class IntType(val width: Width, val signed: Boolean) : Type {
    enum class Width {
        I8,
        I16,
        I32,
        I64,
    }
}
