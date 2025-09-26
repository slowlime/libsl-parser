package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.Annotatable
import org.jetbrains.research.libsl2.ast.LibSLAnnotation
import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.expr.Expr
import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Binding
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity

data class VariableDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var mutable: Boolean,
    var name: Name,
    var typeExpr: TypeExpr,
    var init: Expr?,
) : GlobalDecl, StructMemberDecl, AutomatonMemberDecl, Annotatable, Entity<Binding> {
    override lateinit var primaryDef: Def.Primary<Binding>
    internal val primaryDefInitialized: Boolean
        get() = this::primaryDef.isInitialized
}

fun VariableDecl.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    visitor.visit(typeExpr)
    init?.let(visitor::visit)
}
