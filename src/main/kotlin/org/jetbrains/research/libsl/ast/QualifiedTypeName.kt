package org.jetbrains.research.libsl.ast

data class QualifiedTypeName(
    var typeName: FullName,
    var generics: MutableList<Generic>,
)
