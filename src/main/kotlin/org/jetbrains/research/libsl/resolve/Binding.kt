package org.jetbrains.research.libsl.resolve

import org.jetbrains.research.libsl.ast.decl.ActionDecl
import org.jetbrains.research.libsl.ast.decl.AnnotationDecl
import org.jetbrains.research.libsl.ast.decl.EnumDecl
import org.jetbrains.research.libsl.ast.decl.SemanticTypeDecl

sealed interface Binding {
    data class VariableDecl(val decl: org.jetbrains.research.libsl.ast.decl.VariableDecl) : Binding
    data class ActionParam(val param: ActionDecl.Param) : Binding
    data class AnnotationParam(val param: AnnotationDecl.Param) : Binding
    data class EnumVariant(val variant: EnumDecl.Variant) : Binding
    data class FunctionParam(val param: org.jetbrains.research.libsl.ast.FunctionParam) : Binding
    data class EnumSemanticTypeValue(val value: SemanticTypeDecl.Value) : Binding
    data class StateDecl(val decl: org.jetbrains.research.libsl.ast.decl.StateDecl) : Binding

    companion object {
        fun of(decl: org.jetbrains.research.libsl.ast.decl.VariableDecl): VariableDecl = VariableDecl(decl)
        fun of(param: ActionDecl.Param): ActionParam = ActionParam(param)
        fun of(param: AnnotationDecl.Param): AnnotationParam = AnnotationParam(param)
        fun of(variant: EnumDecl.Variant): EnumVariant = EnumVariant(variant)
        fun of(param: org.jetbrains.research.libsl.ast.FunctionParam): FunctionParam = FunctionParam(param)
        fun of(value: SemanticTypeDecl.Value): EnumSemanticTypeValue = EnumSemanticTypeValue(value)
        fun of(decl: org.jetbrains.research.libsl.ast.decl.StateDecl): StateDecl = StateDecl(decl)
    }
}
