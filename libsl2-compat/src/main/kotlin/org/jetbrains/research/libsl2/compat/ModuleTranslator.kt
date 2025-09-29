package org.jetbrains.research.libsl2.compat

import org.jetbrains.research.libsl.context.ActionContext
import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.nodes.ActionArgumentDescriptor
import org.jetbrains.research.libsl.nodes.ActionDecl
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.AnnotationArgumentDescriptor
import org.jetbrains.research.libsl.nodes.AnnotationUsage
import org.jetbrains.research.libsl.nodes.Automaton
import org.jetbrains.research.libsl.nodes.Expression
import org.jetbrains.research.libsl.nodes.Library
import org.jetbrains.research.libsl.nodes.LslVersion
import org.jetbrains.research.libsl.nodes.MetaNode
import org.jetbrains.research.libsl.nodes.NamedArgumentWithValue
import org.jetbrains.research.libsl.nodes.VariableKind
import org.jetbrains.research.libsl.nodes.VariableWithInitialValue
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.builders.AnnotationReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference
import org.jetbrains.research.libsl.type.GenericType
import org.jetbrains.research.libsl.utils.EntityPosition
import org.jetbrains.research.libsl.utils.PositionInfo
import org.jetbrains.research.libsl2.ast.Header
import org.jetbrains.research.libsl2.ast.LibSLAnnotation
import org.jetbrains.research.libsl2.ast.Module
import org.jetbrains.research.libsl2.ast.decl.GlobalDecl
import org.jetbrains.research.libsl2.location.Location

internal class ModuleTranslator(private val compat: LibSLCompat, private val module: Module) {
    private val library = Library(
        fileName = module.location?.path?.toString() ?: "<unknown>",
        metadata = module.header?.let<Header, MetaNode> { header ->
            MetaNode(
                lslVersion = LslVersion.fromString(header.libslVersion),
                name = header.libraryName,
                libraryVersion = header.version,
                language = header.language,
                url = header.url,
            )
        },
    )

    private val importsDedup = mutableSetOf<String>()
    private val imports = mutableListOf<Pair<String, Module>>()

    data class TranslatedLibrary(val library: Library, val imports: List<Pair<String, Module>>)

    fun translate(): TranslatedLibrary {
        for (decl in module.decls) {
            TopLevelDeclTranslator().translateDecl(decl)
        }

        return TranslatedLibrary(library, imports)
    }

    fun Location.toEntityPosition(): EntityPosition = EntityPosition(
        path.toString(),
        PositionInfo(line, column),
        // libsl2.ast.Location does not store the end position, since ANTLR does not provide a way to obtain it
        PositionInfo(line, column),
    )

    private open inner class Translator<C : LslContextBase>(val ctx: C) {
        fun translateAnnotation(annotation: LibSLAnnotation): AnnotationUsage {
            val name = annotation.name.toString()
            val args = annotation.args.map { arg ->
                val name = arg.name?.toString()
                val value = ExprTranslator(ctx).translateExpr(arg.expr)

                NamedArgumentWithValue(name, value, arg.expr.location!!.toEntityPosition())
            }
            val argTypes = args.map { arg ->
                ctx.typeInferrer.getExpressionType(arg.value).getReference(ctx)
            }
            val annotationRef = AnnotationReferenceBuilder.build(name, argTypes, ctx)

            return AnnotationUsage(
                annotationRef,
                args,
                annotation.location!!.toEntityPosition(),
            )
        }

        fun translateGenerics(
            generics: List<org.jetbrains.research.libsl2.ast.Generic>,
            typeConstraints: List<org.jetbrains.research.libsl2.ast.TypeConstraint>,
        ): MutableList<GenericType> {
            TODO()
        }
    }

    private inner class TopLevelDeclTranslator : Translator<LslGlobalContext>(compat.globalCtx) {
        fun translateDecl(decl: GlobalDecl) {
            when (decl) {
                is org.jetbrains.research.libsl2.ast.decl.ActionDecl -> translateActionDecl(decl)
                is org.jetbrains.research.libsl2.ast.decl.AnnotationDecl -> translateAnnotationDecl(decl)
                is org.jetbrains.research.libsl2.ast.decl.AutomatonDecl -> translateAutomatonDecl(decl)
                is org.jetbrains.research.libsl2.ast.decl.EnumDecl -> translateEnumDecl(decl)
                is org.jetbrains.research.libsl2.ast.decl.FunctionDecl -> translateFunctionDecl(decl)
                is org.jetbrains.research.libsl2.ast.decl.ImportDecl -> translateImportDecl(decl)
                is org.jetbrains.research.libsl2.ast.decl.IncludeDecl -> translateIncludeDecl(decl)
                is org.jetbrains.research.libsl2.ast.decl.SemanticTypeDecl.Enumerated -> translateSemanticTypeDecl(decl)
                is org.jetbrains.research.libsl2.ast.decl.SemanticTypeDecl.Simple -> translateSemanticTypeDecl(decl)
                is org.jetbrains.research.libsl2.ast.decl.StructDecl -> translateStructDecl(decl)
                is org.jetbrains.research.libsl2.ast.decl.TypeAliasDecl -> translateTypeAliasDecl(decl)
                is org.jetbrains.research.libsl2.ast.decl.VariableDecl -> translateVariableDecl(decl)
            }
        }

        fun translateActionDecl(decl: org.jetbrains.research.libsl2.ast.decl.ActionDecl) {
            ActionTranslator(decl).translate()
        }

        fun translateAnnotationDecl(decl: org.jetbrains.research.libsl2.ast.decl.AnnotationDecl) {
            val name = decl.name.toString()

            val params = decl.params.mapTo(mutableListOf()) { param ->
                AnnotationArgumentDescriptor(
                    param.name.toString(),
                    TypeTranslator(compat.globalCtx).translateTypeExpr(param.typeExpr),
                    param.default?.let { ExprTranslator(compat.globalCtx).translateExpr(it) },
                    param.name.location!!.toEntityPosition(),
                )
            }

            val annotation = Annotation(name, params, decl.location!!.toEntityPosition())
            compat.globalCtx.storeAnnotation(annotation)
        }

        fun translateAutomatonDecl(decl: org.jetbrains.research.libsl2.ast.decl.AutomatonDecl) {
            AutomatonTranslator(decl).translate()
        }

        fun translateEnumDecl(decl: org.jetbrains.research.libsl2.ast.decl.EnumDecl) {
            TypeTranslator(compat.globalCtx).translateEnumDecl(decl)
        }

        fun translateFunctionDecl(decl: org.jetbrains.research.libsl2.ast.decl.FunctionDecl) {
            val extensionFor = decl.extensionFor
            val parentCtx = if (extensionFor != null) {
                val automatonRef = AutomatonReferenceBuilder.build(extensionFor.toString(), compat.globalCtx)
                compat.globalCtx.resolveAutomaton(automatonRef)!!.context
            } else {
                compat.globalCtx
            }

            FunctionTranslator(decl, parentCtx, automaton = null).translate()
        }

        fun translateImportDecl(decl: org.jetbrains.research.libsl2.ast.decl.ImportDecl) {
            if (importsDedup.add(decl.path)) {
                library.importNames += decl.path
                imports += Pair(decl.path, decl.importedModule)
            }
        }

        fun translateIncludeDecl(decl: org.jetbrains.research.libsl2.ast.decl.IncludeDecl) {
            library.includes += decl.path
        }

        fun translateSemanticTypeDecl(decl: org.jetbrains.research.libsl2.ast.decl.SemanticTypeDecl.Simple) {
            TypeTranslator(compat.globalCtx).translateSemanticTypeDecl(decl)
        }

        fun translateSemanticTypeDecl(decl: org.jetbrains.research.libsl2.ast.decl.SemanticTypeDecl.Enumerated) {
            TypeTranslator(compat.globalCtx).translateSemanticTypeDecl(decl)
        }

        fun translateStructDecl(decl: org.jetbrains.research.libsl2.ast.decl.StructDecl) {
            TypeTranslator(compat.globalCtx).translateStructDecl(decl)
        }

        fun translateTypeAliasDecl(decl: org.jetbrains.research.libsl2.ast.decl.TypeAliasDecl) {
            TypeTranslator(compat.globalCtx).translateTypeAliasDecl(decl)
        }

        fun translateVariableDecl(decl: org.jetbrains.research.libsl2.ast.decl.VariableDecl) {
            val keyword = if (decl.mutable) VariableKind.VAR else VariableKind.VAL
            val name = decl.name.toString()
            val typeRef = TypeTranslator(compat.globalCtx).translateTypeExpr(decl.typeExpr)
            val initValue = decl.init?.let { ExprTranslator(compat.globalCtx).translateExpr(it) }
            val annotations = decl.annotations.mapTo(mutableListOf(), ::translateAnnotation)
            val variable = VariableWithInitialValue(
                keyword,
                name,
                typeRef,
                annotations,
                initValue,
                decl.location!!.toEntityPosition(),
            )
            compat.globalCtx.storeVariable(variable)
        }
    }

    private inner class AutomatonTranslator(val decl: org.jetbrains.research.libsl2.ast.decl.AutomatonDecl) :
        Translator<AutomatonContext>(AutomatonContext(compat.globalCtx)) {

        fun translate() {
            TODO()
        }
    }

    private inner class FunctionTranslator(
        val decl: org.jetbrains.research.libsl2.ast.decl.FunctionLikeDecl,
        parentCtx: LslContextBase,
        var automaton: Automaton?,
    ) : Translator<FunctionContext>(FunctionContext(parentCtx)) {
        fun translate() {
            when (decl) {
                is org.jetbrains.research.libsl2.ast.decl.ConstructorDecl -> translate(decl)
                is org.jetbrains.research.libsl2.ast.decl.DestructorDecl -> translate(decl)
                is org.jetbrains.research.libsl2.ast.decl.FunctionDecl -> translate(decl)
                is org.jetbrains.research.libsl2.ast.decl.ProcDecl -> translate(decl)
            }
        }

        private fun translate(decl: org.jetbrains.research.libsl2.ast.decl.ConstructorDecl) {
            TODO()
        }

        private fun translate(decl: org.jetbrains.research.libsl2.ast.decl.DestructorDecl) {
            TODO()
        }

        private fun translate(decl: org.jetbrains.research.libsl2.ast.decl.FunctionDecl) {
            TODO()
        }

        private fun translate(decl: org.jetbrains.research.libsl2.ast.decl.ProcDecl) {
            TODO()
        }
    }

    private inner class ActionTranslator(val decl: org.jetbrains.research.libsl2.ast.decl.ActionDecl) :
        Translator<ActionContext>(ActionContext(compat.globalCtx)) {

        fun translate() {
            val name = decl.name.toString()

            val params = decl.params.mapTo(mutableListOf()) { param ->
                ActionArgumentDescriptor(
                    param.annotations.mapTo(mutableListOf(), ::translateAnnotation),
                    param.name.toString(),
                    TypeTranslator(ctx).translateTypeExpr(param.typeExpr),
                    param.name.location!!.toEntityPosition(),
                )
            }

            val returnType = decl.returnType?.let { TypeTranslator(ctx).translateTypeExpr(it) }
            val annotations = decl.annotations.mapTo(mutableListOf(), ::translateAnnotation)

            for (genericType in translateGenerics(decl.generics, decl.typeConstraints)) {
                ctx.storeActionType(genericType)
            }

            val action = ActionDecl(
                name,
                params,
                annotations,
                ctx,
                returnType,
                decl.location!!.toEntityPosition(),
            )

            compat.globalCtx.storeDeclaredAction(action)
        }
    }

    private inner class TypeTranslator(ctx: LslContextBase) : Translator<LslContextBase>(ctx) {
        fun translateStructDecl(decl: org.jetbrains.research.libsl2.ast.decl.StructDecl): TypeReference {
            TODO()
        }

        fun translateSemanticTypeDecl(decl: org.jetbrains.research.libsl2.ast.decl.SemanticTypeDecl.Simple): TypeReference {
            TODO()
        }

        fun translateSemanticTypeDecl(decl: org.jetbrains.research.libsl2.ast.decl.SemanticTypeDecl.Enumerated): TypeReference {
            TODO()
        }

        fun translateTypeAliasDecl(decl: org.jetbrains.research.libsl2.ast.decl.TypeAliasDecl): TypeReference {
            TODO()
        }

        fun translateEnumDecl(decl: org.jetbrains.research.libsl2.ast.decl.EnumDecl): TypeReference {
            TODO()
        }

        fun translateTypeExpr(typeExpr: org.jetbrains.research.libsl2.ast.type.TypeExpr): TypeReference {
            TODO()
        }
    }

    private inner class ExprTranslator(ctx: LslContextBase) : Translator<LslContextBase>(ctx) {
        fun translateExpr(expr: org.jetbrains.research.libsl2.ast.expr.Expr): Expression {
            TODO()
        }
    }
}
