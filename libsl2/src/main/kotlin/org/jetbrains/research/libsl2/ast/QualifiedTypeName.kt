package org.jetbrains.research.libsl2.ast

data class QualifiedTypeName(
    var typeName: FullName,
    var generics: MutableList<Generic>,
)
