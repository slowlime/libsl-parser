package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.LocationProvider

sealed interface Decl : LocationProvider

fun Decl.walk(visitor: Visitor) {
    when (this) {
        is ConstructorDecl -> visitor.visit(this)
        is DestructorDecl -> visitor.visit(this)
        is FunctionDecl -> visitor.visit(this)
        is ProcDecl -> visitor.visit(this)
        is ShiftDecl -> visitor.visit(this)
        is StateDecl -> visitor.visit(this)
        is VariableDecl -> visitor.visit(this)
        is ActionDecl -> visitor.visit(this)
        is AnnotationDecl -> visitor.visit(this)
        is AutomatonDecl -> visitor.visit(this)
        is EnumDecl -> visitor.visit(this)
        is ImportDecl -> visitor.visit(this)
        is IncludeDecl -> visitor.visit(this)
        is SemanticTypeDecl -> visitor.visit(this)
        is StructDecl -> visitor.visit(this)
        is TypeAliasDecl -> visitor.visit(this)
        is StructMemberDecl -> visitor.visit(this)
    }
}
