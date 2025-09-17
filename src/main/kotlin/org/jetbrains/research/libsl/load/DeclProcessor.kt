package org.jetbrains.research.libsl.load

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.ast.FunctionBody
import org.jetbrains.research.libsl.ast.FunctionParam
import org.jetbrains.research.libsl.ast.decl.ActionDecl
import org.jetbrains.research.libsl.ast.decl.AnnotationDecl
import org.jetbrains.research.libsl.ast.decl.AutomatonDecl
import org.jetbrains.research.libsl.ast.decl.AutomatonMemberDecl
import org.jetbrains.research.libsl.ast.decl.EnumDecl
import org.jetbrains.research.libsl.ast.decl.FunctionDecl
import org.jetbrains.research.libsl.ast.decl.ImportDecl
import org.jetbrains.research.libsl.ast.decl.IncludeDecl
import org.jetbrains.research.libsl.ast.decl.SemanticTypeDecl
import org.jetbrains.research.libsl.ast.decl.StructDecl
import org.jetbrains.research.libsl.ast.decl.StructMemberDecl
import org.jetbrains.research.libsl.ast.decl.TypeAliasDecl
import org.jetbrains.research.libsl.ast.decl.VariableDecl

internal class DeclProcessor(private val loader: ModuleLoader) {
    fun process(ctx: LibSLParser.ImportDeclContext): ImportDecl = ImportDecl(
        loader.locationOf(ctx),
        loader.processPath(ctx.path()),
    )

    fun process(ctx: LibSLParser.IncludeDeclContext): IncludeDecl = IncludeDecl(
        loader.locationOf(ctx),
        loader.processPath(ctx.path()),
    )

    fun process(ctx: LibSLParser.SemanticTypeDeclContext): SemanticTypeDecl {
        val annotations = loader.processAnnotations(ctx.annotations)
        val typeName = loader.processQualifiedTypeName(ctx.typeName)
        val realType = loader.processTypeExpr(ctx.realType)

        return when (val def = ctx.semanticTypeDef()) {
            is LibSLParser.SemanticTypeDefSimpleContext -> SemanticTypeDecl.Simple(
                loader.locationOf(ctx),
                annotations,
                typeName,
                realType,
            )

            is LibSLParser.SemanticTypeDefEnumContext -> SemanticTypeDecl.Enumerated(
                loader.locationOf(ctx),
                annotations,
                typeName,
                realType,
                def.values.mapToMutable { value ->
                    SemanticTypeDecl.Value(
                        loader.processName(value.name),
                        loader.processAtomicExpr(value.value),
                    )
                },
            )

            else -> error("unknown semantic type def $ctx")
        }
    }

    fun process(ctx: LibSLParser.TypeAliasDeclContext): TypeAliasDecl = TypeAliasDecl(
        loader.locationOf(ctx),
        loader.processAnnotations(ctx.annotations),
        loader.processQualifiedTypeName(ctx.typeName),
        loader.processTypeExpr(ctx.def),
    )

    fun process(ctx: LibSLParser.StructDeclContext): StructDecl = StructDecl(
        loader.locationOf(ctx),
        loader.processAnnotations(ctx.annotations),
        loader.processQualifiedTypeName(ctx.typeName),
        ctx.targetType?.isType?.let(loader::processTypeExpr),
        ctx.targetType?.forTypes?.typeExprs.mapToMutable(loader::processTypeExpr),
        ctx.typeConstraints?.let(loader::processWhereClause).orEmptyMutable(),
        ctx.decls.mapToMutable(::processStructMemberDecl),
    )

    fun process(ctx: LibSLParser.EnumDeclContext): EnumDecl = EnumDecl(
        loader.locationOf(ctx),
        loader.processAnnotations(ctx.annotations),
        loader.processQualifiedTypeName(ctx.typeName),
        ctx.variants.mapToMutable { variant ->
            EnumDecl.Variant(loader.processName(variant.name), loader.processSignedIntLit(variant.value))
        },
    )

    fun process(ctx: LibSLParser.AnnotationDeclContext): AnnotationDecl = AnnotationDecl(
        loader.locationOf(ctx),
        loader.processName(ctx.name),
        ctx.params?.params.mapToMutable { param ->
            AnnotationDecl.Param(
                loader.processName(param.name),
                loader.processTypeExpr(param.type),
                param.default_?.let(loader::processExpr),
            )
        },
    )

    fun process(ctx: LibSLParser.ActionDeclContext): ActionDecl = ActionDecl(
        loader.locationOf(ctx),
        loader.processAnnotations(ctx.annotations),
        loader.processName(ctx.name),
        ctx.typeParams?.let(loader::processGenerics).orEmptyMutable(),
        ctx.params?.params.mapToMutable { param ->
            ActionDecl.Param(
                loader.processAnnotations(param.annotations),
                loader.processName(param.name),
                loader.processTypeExpr(param.type),
            )
        },
        ctx.retType?.let(loader::processTypeExpr),
        ctx.typeConstrants?.let(loader::processWhereClause).orEmptyMutable(),
    )

    fun process(ctx: LibSLParser.AutomatonDeclContext): AutomatonDecl = AutomatonDecl(
        loader.locationOf(ctx),
        loader.processAnnotations(ctx.annotations),
        ctx.concept != null,
        loader.processQualifiedTypeName(ctx.name),
        ctx.constructorVariables?.variables.mapToMutable(::processConstructorVariable),
        loader.processTypeExpr(ctx.type),
        ctx.implements_?.concepts.mapToMutable(loader::processName),
        ctx.decls.mapToMutable(::processAutomatonMemberDecl),
    )

    fun process(ctx: LibSLParser.FunctionDeclContext): FunctionDecl = FunctionDecl(
        loader.locationOf(ctx),
        loader.processAnnotations(ctx.annotations),
        ctx.static_ != null,
        ctx.extensionFor?.let(loader::processFullName),
        ctx.method != null,
        loader.processName(ctx.name),
        ctx.typeParams?.let(loader::processGenerics).orEmptyMutable(),
        ctx.params?.params.mapToMutable(::processFunctionParam),
        ctx.retType?.let(loader::processTypeExpr),
        ctx.typeConstraints?.let(loader::processWhereClause).orEmptyMutable(),
        ctx.def?.let(::processFunctionDef),
    )

    fun process(ctx: LibSLParser.VariableDeclContext): VariableDecl = VariableDecl(
        loader.locationOf(ctx),
        loader.processAnnotations(ctx.annotations),
        ctx.kind is LibSLParser.VariableKindVarContext,
        loader.processTypeExpr(ctx.type),
        ctx.init?.let(loader::processExpr),
    )

    private fun processStructMemberDecl(ctx: LibSLParser.StructDefDeclContext): StructMemberDecl = TODO()

    private fun processConstructorVariable(ctx: LibSLParser.ConstructorVariableContext): VariableDecl = TODO()

    private fun processAutomatonMemberDecl(ctx: LibSLParser.AutomatonDefDeclContext): AutomatonMemberDecl = TODO()

    private fun processFunctionParam(ctx: LibSLParser.FunctionParamContext): FunctionParam = TODO()

    private fun processFunctionDef(ctx: LibSLParser.FunctionDefContext): FunctionBody? = TODO()
}
