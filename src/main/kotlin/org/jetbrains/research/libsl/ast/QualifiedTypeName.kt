package org.jetbrains.research.libsl.ast

data class QualifiedTypeName(
    val typeName: FullName,
    val generics: MutableList<Generic>,
)
