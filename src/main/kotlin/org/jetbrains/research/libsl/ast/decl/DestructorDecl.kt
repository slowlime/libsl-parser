package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.FunctionBody
import org.jetbrains.research.libsl.ast.decl.FunctionLikeDecl
import org.jetbrains.research.libsl.ast.FunctionParam
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.ast.walk
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.resolve.Entity
import org.jetbrains.research.libsl.resolve.scope.MutableScope

data class DestructorDecl(
    override var location: Location?,
    override var annotations: MutableList<LibSLAnnotation>,
    var isMethod: Boolean,
    override var name: Name,
    override var params: MutableList<FunctionParam>,
    override var returnType: TypeExpr?,
    override var body: FunctionBody?,
) : FunctionLikeDecl, AutomatonMemberDecl, Entity<FunctionLikeDecl> {
    override lateinit var primaryDef: Def.Primary<FunctionLikeDecl>
    lateinit var paramScope: MutableScope
}

fun DestructorDecl.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    for (param in params) {
        param.walk(visitor)
    }

    returnType?.let(visitor::visit)
    body?.walk(visitor)
}
