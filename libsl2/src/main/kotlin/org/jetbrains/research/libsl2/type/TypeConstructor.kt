package org.jetbrains.research.libsl2.type

import org.jetbrains.research.libsl2.ast.decl.EnumDecl
import org.jetbrains.research.libsl2.ast.decl.SemanticTypeDecl
import org.jetbrains.research.libsl2.ast.decl.StructDecl
import org.jetbrains.research.libsl2.ast.decl.TypeAliasDecl

sealed interface TypeConstructor {
    val params: List<TypeParam>?

    class Nullary(val type: Type) : TypeConstructor {
        override val params: List<TypeParam>? = null
    }

    sealed interface NonNullary : TypeConstructor {
        override val params: List<TypeParam>
    }

    class Alias(val decl: TypeAliasDecl) : NonNullary {
        override val params: MutableList<TypeParam> = mutableListOf()
    }

    class Struct(val decl: StructDecl) : NonNullary {
        override val params: MutableList<TypeParam> = mutableListOf()
    }

    class Enum(val decl: EnumDecl) : NonNullary {
        override val params: MutableList<TypeParam> = mutableListOf()
    }

    class Semantic(val decl: SemanticTypeDecl) : NonNullary {
        override val params: MutableList<TypeParam> = mutableListOf()
    }
}

class ConstructedType(val constructor: TypeConstructor.NonNullary, val args: MutableList<Arg>) : Type {
    class Arg(val type: Type?) {
        fun isWildcard(): Boolean = type == null
    }
}
