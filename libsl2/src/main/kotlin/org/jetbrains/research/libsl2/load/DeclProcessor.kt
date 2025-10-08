package org.jetbrains.research.libsl2.load

import org.jetbrains.research.libsl2.LibSLParser
import org.jetbrains.research.libsl2.ast.FunctionBody
import org.jetbrains.research.libsl2.ast.FunctionParam
import org.jetbrains.research.libsl2.ast.FunctionSignature
import org.jetbrains.research.libsl2.ast.decl.ActionDecl
import org.jetbrains.research.libsl2.ast.decl.AnnotationDecl
import org.jetbrains.research.libsl2.ast.decl.AutomatonDecl
import org.jetbrains.research.libsl2.ast.decl.AutomatonMemberDecl
import org.jetbrains.research.libsl2.ast.decl.ConstructorDecl
import org.jetbrains.research.libsl2.ast.decl.DestructorDecl
import org.jetbrains.research.libsl2.ast.decl.EnumDecl
import org.jetbrains.research.libsl2.ast.decl.FunctionDecl
import org.jetbrains.research.libsl2.ast.decl.ImportDecl
import org.jetbrains.research.libsl2.ast.decl.IncludeDecl
import org.jetbrains.research.libsl2.ast.decl.ProcDecl
import org.jetbrains.research.libsl2.ast.decl.SemanticTypeDecl
import org.jetbrains.research.libsl2.ast.decl.ShiftDecl
import org.jetbrains.research.libsl2.ast.decl.StateDecl
import org.jetbrains.research.libsl2.ast.decl.StructDecl
import org.jetbrains.research.libsl2.ast.decl.StructMemberDecl
import org.jetbrains.research.libsl2.ast.decl.TypeAliasDecl
import org.jetbrains.research.libsl2.ast.decl.VariableDecl
import org.jetbrains.research.libsl2.location.LoadChain

internal class DeclProcessor(private val loader: ModuleLoader) {
    fun process(ctx: LibSLParser.ImportDeclContext): ImportDecl {
        val location = loader.locationOf(ctx)
        val path = loader.processPath(ctx.path())
        val decl = ImportDecl(location, path)

        loader.libsl.requestLoad(path, LoadChain.Imported(location)) { module ->
            decl.importedModule = module
        }

        return decl
    }

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

            else -> error("unrecognized semantic type def $ctx")
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
        ctx.typeConstraints?.let(loader::processWhereClause).orEmptyMutable(),
        ctx.decls.flatMapToMutable(::processAutomatonMemberDecl),
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
        loader.processName(ctx.name),
        loader.processTypeExpr(ctx.type),
        ctx.init?.let(loader::processExpr),
    )

    fun process(ctx: LibSLParser.StateDeclContext): List<StateDecl> = ctx.names.names.map { name ->
        StateDecl(
            loader.locationOf(ctx),
            when (ctx.kind) {
                is LibSLParser.StateKindInitialContext -> StateDecl.Kind.Initial
                is LibSLParser.StateKindRegularContext -> StateDecl.Kind.Regular
                is LibSLParser.StateKindFinalContext -> StateDecl.Kind.Final
                else -> error("unrecognized state kind $ctx")
            },
            loader.processName(name),
        )
    }

    fun process(ctx: LibSLParser.ShiftDeclContext): ShiftDecl = ShiftDecl(
        loader.locationOf(ctx),
        when (val from = ctx.from) {
            is LibSLParser.ShiftSourceStateShorthandContext -> mutableListOf(loader.processName(from.ident()))
            is LibSLParser.ShiftSourceStateListContext -> from.states?.names.mapToMutable(loader::processName)
            else -> error("unrecognized shift source state $ctx")
        },
        loader.processName(ctx.to),
        when (val by = ctx.by) {
            is LibSLParser.ShiftByShorthandContext -> mutableListOf(processFunctionSignature(by.signature))
            is LibSLParser.ShiftByListContext -> by.signatures?.signatures.mapToMutable(::processFunctionSignature)
            else -> error("unrecognized shift edge $ctx")
        },
    )

    fun process(ctx: LibSLParser.ConstructorDeclContext): ConstructorDecl = ConstructorDecl(
        loader.locationOf(ctx),
        loader.processAnnotations(ctx.annotations),
        ctx.method != null,
        loader.processName(ctx.name),
        ctx.params?.params.mapToMutable(::processFunctionParam),
        ctx.retType?.let(loader::processTypeExpr),
        ctx.def?.let(::processFunctionDef),
    )

    fun process(ctx: LibSLParser.DestructorDeclContext): DestructorDecl = DestructorDecl(
        loader.locationOf(ctx),
        loader.processAnnotations(ctx.annotations),
        ctx.method != null,
        loader.processName(ctx.name),
        ctx.params?.params.mapToMutable(::processFunctionParam),
        ctx.retType?.let(loader::processTypeExpr),
        ctx.def?.let(::processFunctionDef),
    )

    fun process(ctx: LibSLParser.ProcDeclContext): ProcDecl = ProcDecl(
        loader.locationOf(ctx),
        loader.processAnnotations(ctx.annotations),
        ctx.method != null,
        loader.processName(ctx.name),
        ctx.typeParams?.let(loader::processGenerics).orEmptyMutable(),
        ctx.params?.params.mapToMutable(::processFunctionParam),
        ctx.retType?.let(loader::processTypeExpr),
        ctx.typeConstraints?.let(loader::processWhereClause).orEmptyMutable(),
        ctx.def?.let(::processFunctionDef),
    )

    private fun processStructMemberDecl(ctx: LibSLParser.StructDefDeclContext): StructMemberDecl = when (ctx) {
        is LibSLParser.StructDefDeclFunctionContext -> process(ctx.functionDecl())
        is LibSLParser.StructDefDeclVariableContext -> process(ctx.variableDecl())
        else -> error("unrecognized struct member decl $ctx")
    }

    private fun processConstructorVariable(ctx: LibSLParser.ConstructorVariableContext): VariableDecl = VariableDecl(
        loader.locationOf(ctx),
        loader.processAnnotations(ctx.annotations),
        ctx.kind is LibSLParser.VariableKindVarContext,
        loader.processName(ctx.name),
        loader.processTypeExpr(ctx.type),
        ctx.init?.let(loader::processExpr),
    )

    private fun processAutomatonMemberDecl(ctx: LibSLParser.AutomatonDefDeclContext): Iterable<AutomatonMemberDecl> =
        when (ctx) {
            is LibSLParser.AutomatonDefDeclStateContext -> process(ctx.stateDecl())
            is LibSLParser.AutomatonDefDeclShiftContext -> listOf(process(ctx.shiftDecl()))
            is LibSLParser.AutomatonDefDeclConstructorContext -> listOf(process(ctx.constructorDecl()))
            is LibSLParser.AutomatonDefDeclDestructorContext -> listOf(process(ctx.destructorDecl()))
            is LibSLParser.AutomatonDefDeclProcContext -> listOf(process(ctx.procDecl()))
            is LibSLParser.AutomatonDefDeclFunctionContext -> listOf(process(ctx.functionDecl()))
            is LibSLParser.AutomatonDefDeclVariableContext -> listOf(process(ctx.variableDecl()))
            else -> error("unrecognized automaton member decl $ctx")
        }

    private fun processFunctionParam(ctx: LibSLParser.FunctionParamContext): FunctionParam = FunctionParam(
        loader.processAnnotations(ctx.annotations),
        loader.processName(ctx.name),
        loader.processTypeExpr(ctx.type),
    )

    private fun processFunctionDef(ctx: LibSLParser.FunctionDefContext): FunctionBody? = when (ctx) {
        is LibSLParser.FunctionDefSemicolonContext -> null
        is LibSLParser.FunctionDefBracedContext -> processFunctionBody(ctx.body)
        else -> error("unrecognized function def $ctx")
    }

    private fun processFunctionBody(ctx: LibSLParser.FunctionBodyContext): FunctionBody = FunctionBody(
        ctx.contracts.mapToMutable(loader::processContract),
        ctx.stmts.mapToMutable(loader::processStmt),
    )

    private fun processFunctionSignature(ctx: LibSLParser.FunctionSignatureContext): FunctionSignature = when (ctx) {
        is LibSLParser.FunctionSignatureShorthandContext -> FunctionSignature(
            loader.processName(ctx.name),
            mutableListOf(),
        )

        is LibSLParser.FunctionSignatureQualifiedContext -> FunctionSignature(
            loader.processName(ctx.name),
            ctx.params?.typeExprs.mapToMutable(loader::processTypeExpr),
        )

        else -> error("unrecognized function signature $ctx")
    }
}
