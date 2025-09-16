package org.jetbrains.research.libsl.load

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.ast.decl.ActionDecl
import org.jetbrains.research.libsl.ast.decl.AnnotationDecl
import org.jetbrains.research.libsl.ast.decl.AutomatonDecl
import org.jetbrains.research.libsl.ast.decl.EnumDecl
import org.jetbrains.research.libsl.ast.decl.FunctionDecl
import org.jetbrains.research.libsl.ast.decl.ImportDecl
import org.jetbrains.research.libsl.ast.decl.IncludeDecl
import org.jetbrains.research.libsl.ast.decl.SemanticTypeDecl
import org.jetbrains.research.libsl.ast.decl.StructDecl
import org.jetbrains.research.libsl.ast.decl.TypeAliasDecl
import org.jetbrains.research.libsl.ast.decl.VariableDecl

internal class DeclProcessor(private val loader: ModuleLoader) {
    fun process(ctx: LibSLParser.ImportDeclContext): ImportDecl = TODO()

    fun process(ctx: LibSLParser.IncludeDeclContext): IncludeDecl = TODO()

    fun process(ctx: LibSLParser.SemanticTypeDeclContext): SemanticTypeDecl = TODO()

    fun process(ctx: LibSLParser.TypeAliasDeclContext): TypeAliasDecl = TODO()

    fun process(ctx: LibSLParser.StructDeclContext): StructDecl = TODO()

    fun process(ctx: LibSLParser.EnumDeclContext): EnumDecl = TODO()

    fun process(ctx: LibSLParser.AnnotationDeclContext): AnnotationDecl = TODO()

    fun process(ctx: LibSLParser.ActionDeclContext): ActionDecl = TODO()

    fun process(ctx: LibSLParser.AutomatonDeclContext): AutomatonDecl = TODO()

    fun process(ctx: LibSLParser.FunctionDeclContext): FunctionDecl = TODO()

    fun process(ctx: LibSLParser.VariableDeclContext): VariableDecl = TODO()
}
