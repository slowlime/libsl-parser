package org.jetbrains.research.libsl2.resolve

import org.jetbrains.research.libsl2.ast.decl.ActionDecl
import org.jetbrains.research.libsl2.ast.decl.AnnotationDecl
import org.jetbrains.research.libsl2.ast.decl.EnumDecl
import org.jetbrains.research.libsl2.ast.decl.SemanticTypeDecl
import org.jetbrains.research.libsl2.ast.predicate.NamedPredicate

sealed interface Binding {
    data class VariableDecl(val decl: org.jetbrains.research.libsl2.ast.decl.VariableDecl) : Binding
    data class ActionParam(val param: ActionDecl.Param) : Binding
    data class AnnotationParam(val param: AnnotationDecl.Param) : Binding
    data class EnumVariant(val variant: EnumDecl.Variant) : Binding
    data class FunctionParam(val param: org.jetbrains.research.libsl2.ast.FunctionParam) : Binding
    data class EnumSemanticTypeValue(val value: SemanticTypeDecl.Value) : Binding
    data class Predicate(val value: NamedPredicate) : Binding

    companion object {
        fun of(decl: org.jetbrains.research.libsl2.ast.decl.VariableDecl): VariableDecl = VariableDecl(decl)
        fun of(param: ActionDecl.Param): ActionParam = ActionParam(param)
        fun of(param: AnnotationDecl.Param): AnnotationParam = AnnotationParam(param)
        fun of(variant: EnumDecl.Variant): EnumVariant = EnumVariant(variant)
        fun of(param: org.jetbrains.research.libsl2.ast.FunctionParam): FunctionParam = FunctionParam(param)
        fun of(value: SemanticTypeDecl.Value): EnumSemanticTypeValue = EnumSemanticTypeValue(value)
        fun of(value: NamedPredicate): Predicate = Predicate(value)
    }
}
