package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Annotatable
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Binding
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity
import org.jetbrains.research.libsl.resolve.scope.MutableScope
import org.jetbrains.research.libsl.type.TypeConstructor

sealed class SemanticTypeDecl : GlobalDecl, Annotatable, Entity<TypeConstructor> {
    abstract var typeName: QualifiedTypeName
    abstract var realType: TypeExpr

    override lateinit var primaryDef: Def.Primary<TypeConstructor>

    data class Simple(
        override var location: Location?,
        override var annotations: MutableList<LibSLAnnotation>,
        override var typeName: QualifiedTypeName,
        override var realType: TypeExpr,
    ) : SemanticTypeDecl() {
        lateinit var scope: MutableScope
    }

    data class Enumerated(
        override var location: Location?,
        override var annotations: MutableList<LibSLAnnotation>,
        override var typeName: QualifiedTypeName,
        override var realType: TypeExpr,
        var values: MutableList<Value>,
    ) : SemanticTypeDecl() {
        lateinit var scope: MutableScope
    }

    data class Value(var name: Name, var expr: Expr) : Entity<Binding> {
        override lateinit var primaryDef: Def.Primary<Binding>
    }
}

fun SemanticTypeDecl.walk(visitor: Visitor) {
    when (this) {
        is SemanticTypeDecl.Enumerated -> visitor.visit(this)
        is SemanticTypeDecl.Simple -> visitor.visit(this)
    }
}

fun SemanticTypeDecl.Simple.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    visitor.visit(realType)
}

fun SemanticTypeDecl.Enumerated.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    visitor.visit(realType)

    for (value in values) {
        value.walk(visitor)
    }
}

fun SemanticTypeDecl.Value.walk(visitor: Visitor) {
    visitor.visit(expr)
}
