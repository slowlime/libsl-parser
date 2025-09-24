package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.FullName
import org.jetbrains.research.libsl.ast.FunctionBody
import org.jetbrains.research.libsl.ast.FunctionLike
import org.jetbrains.research.libsl.ast.FunctionParam
import org.jetbrains.research.libsl.ast.Generic
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.TypeConstraint
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.ast.walk
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity
import org.jetbrains.research.libsl.resolve.scope.MutableScope

data class FunctionDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var isStatic: Boolean,
    var extensionFor: FullName?,
    var isMethod: Boolean,
    override var name: Name,
    var generics: MutableList<Generic>,
    override var params: MutableList<FunctionParam>,
    override var returnType: TypeExpr?,
    var typeConstraints: MutableList<TypeConstraint>,
    override var body: FunctionBody?,
) : FunctionLike, GlobalDecl, StructMemberDecl, AutomatonMemberDecl, Entity<FunctionDecl> {
    override lateinit var primaryDef: Def.Primary<FunctionDecl>
    internal val primaryDefInitialized: Boolean
        get() = this::primaryDef.isInitialized

    lateinit var paramScope: MutableScope
    var resolvedExtensionFor: Def<AutomatonDecl>? = null
}

fun FunctionDecl.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    for (param in params) {
        param.walk(visitor)
    }

    returnType?.let(visitor::visit)

    for (typeConstraint in typeConstraints) {
        typeConstraint.walk(visitor)
    }

    body?.walk(visitor)
}
