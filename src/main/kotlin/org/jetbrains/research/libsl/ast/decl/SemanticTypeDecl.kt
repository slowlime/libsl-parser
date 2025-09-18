package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location

sealed class SemanticTypeDecl : GlobalDecl, Annotatable {
    abstract var typeName: QualifiedTypeName
    abstract var realType: TypeExpr

    data class Simple(
        override var location: Location,
        override var annotations: MutableList<LibSLAnnotation>,
        override var typeName: QualifiedTypeName,
        override var realType: TypeExpr,
    ) : SemanticTypeDecl()

    data class Enumerated(
        override var location: Location,
        override var annotations: MutableList<LibSLAnnotation>,
        override var typeName: QualifiedTypeName,
        override var realType: TypeExpr,
        var values: MutableList<Value>,
    ) : SemanticTypeDecl()

    data class Value(var name: Name, var expr: Expr)
}
