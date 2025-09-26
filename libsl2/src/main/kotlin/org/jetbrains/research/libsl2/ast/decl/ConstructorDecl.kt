package org.jetbrains.research.libsl2.ast.decl

import org.jetbrains.research.libsl2.ast.LibSLAnnotation
import org.jetbrains.research.libsl2.ast.FunctionBody
import org.jetbrains.research.libsl2.ast.decl.FunctionLikeDecl
import org.jetbrains.research.libsl2.ast.FunctionParam
import org.jetbrains.research.libsl2.ast.Name
import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.ast.walk
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.resolve.Def
import org.jetbrains.research.libsl2.resolve.Entity
import org.jetbrains.research.libsl2.resolve.scope.MutableScope

data class ConstructorDecl(
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

fun ConstructorDecl.walk(visitor: Visitor) {
    for (annotation in annotations) {
        visitor.visit(annotation)
    }

    for (param in params) {
        param.walk(visitor)
    }

    returnType?.let(visitor::visit)
    body?.walk(visitor)
}
