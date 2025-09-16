package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.Annotation
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

sealed class SemanticTypeDecl : GlobalDecl, Annotatable {
    abstract val typeName: QualifiedTypeName
    abstract val realType: TypeExpr

    data class Simple(
        override val location: Location,
        override val annotations: MutableList<Annotation>,
        override val typeName: QualifiedTypeName,
        override val realType: TypeExpr,
    ) : SemanticTypeDecl()

    data class Enumerated(
        override val location: Location,
        override val annotations: MutableList<Annotation>,
        override val typeName: QualifiedTypeName,
        override val realType: TypeExpr,
        val values: MutableList<Value>,
    ) : SemanticTypeDecl()

    data class Value(val name: Name, val expr: Expr)
}
