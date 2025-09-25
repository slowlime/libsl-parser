package org.jetbrains.research.libsl.type

import org.jetbrains.research.libsl.ast.decl.EnumDecl
import org.jetbrains.research.libsl.ast.decl.SemanticTypeDecl
import org.jetbrains.research.libsl.ast.decl.StructDecl
import org.jetbrains.research.libsl.ast.decl.TypeAliasDecl

sealed interface TypeConstructor {
    val params: List<TypeParam>?

    class Nullary(val type: Type) : TypeConstructor {
        override val params: List<TypeParam>? = null
    }

    class Alias(val decl: TypeAliasDecl) : TypeConstructor {
        override val params: MutableList<TypeParam> = mutableListOf()
    }

    class Struct(val decl: StructDecl) : TypeConstructor {
        override val params: MutableList<TypeParam> = mutableListOf()
    }

    class Enum(val decl: EnumDecl) : TypeConstructor {
        override val params: MutableList<TypeParam> = mutableListOf()
    }

    class Semantic(val decl: SemanticTypeDecl) : TypeConstructor {
        override val params: MutableList<TypeParam> = mutableListOf()
    }
}

class ConstructedType(val constructor: TypeConstructor, val args: MutableList<Arg>) : Type {
    class Arg(val type: Type?) {
        fun isWildcard(): Boolean = type == null
    }
}
