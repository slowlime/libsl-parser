package org.jetbrains.research.libsl2.compat

import org.jetbrains.research.libsl.context.ActionContext
import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.UnresolvedState
import org.jetbrains.research.libsl.nodes.ActionArgumentDescriptor
import org.jetbrains.research.libsl.nodes.ActionDecl
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.AnnotationArgumentDescriptor
import org.jetbrains.research.libsl.nodes.AnnotationUsage
import org.jetbrains.research.libsl.nodes.AssignOps
import org.jetbrains.research.libsl.nodes.Assignment
import org.jetbrains.research.libsl.nodes.Automaton
import org.jetbrains.research.libsl.nodes.AutomatonConcept
import org.jetbrains.research.libsl.nodes.Constructor
import org.jetbrains.research.libsl.nodes.ConstructorArgument
import org.jetbrains.research.libsl.nodes.Contract
import org.jetbrains.research.libsl.nodes.ContractKind
import org.jetbrains.research.libsl.nodes.Destructor
import org.jetbrains.research.libsl.nodes.ElseStatement
import org.jetbrains.research.libsl.nodes.Expression
import org.jetbrains.research.libsl.nodes.ExpressionStatement
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.nodes.FunctionArgument
import org.jetbrains.research.libsl.nodes.FunctionKind
import org.jetbrains.research.libsl.nodes.IfStatement
import org.jetbrains.research.libsl.nodes.ImplementedConcept
import org.jetbrains.research.libsl.nodes.Library
import org.jetbrains.research.libsl.nodes.LslVersion
import org.jetbrains.research.libsl.nodes.MetaNode
import org.jetbrains.research.libsl.nodes.NamedArgumentWithValue
import org.jetbrains.research.libsl.nodes.Procedure
import org.jetbrains.research.libsl.nodes.QualifiedAccess
import org.jetbrains.research.libsl.nodes.ResultVariable
import org.jetbrains.research.libsl.nodes.Shift
import org.jetbrains.research.libsl.nodes.State
import org.jetbrains.research.libsl.nodes.StateKind
import org.jetbrains.research.libsl.nodes.Statement
import org.jetbrains.research.libsl.nodes.VariableDeclaration
import org.jetbrains.research.libsl.nodes.VariableKind
import org.jetbrains.research.libsl.nodes.VariableWithInitialValue
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.builders.AnnotationReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder.getReference
import org.jetbrains.research.libsl.nodes.references.builders.FunctionReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference
import org.jetbrains.research.libsl.nodes.references.toSimpleString
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
            val ordered = linkedMapOf<String, GenericType>()

            for (generic in generics) {
                ordered[generic.name.toString()] = GenericType(name = generic.name.toString(), context = ctx)
            }

            for (typeConstraint in typeConstraints) {
                val param = typeConstraint.param.toString()
                val constraint = TypeTranslator(ctx).translateTypeExpr(typeConstraint.bound)

                ordered[param]?.constraints += constraint
            }

            return ordered.values.toMutableList()
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
            val init = decl.init?.let { ExprTranslator(compat.globalCtx).translateExpr(it) }
            val annotations = decl.annotations.mapTo(mutableListOf(), ::translateAnnotation)
            val variable = VariableWithInitialValue(
                keyword,
                name,
                typeRef,
                annotations,
                init,
                decl.location!!.toEntityPosition(),
            )
            compat.globalCtx.storeVariable(variable)
        }
    }

    private inner class AutomatonTranslator(val decl: org.jetbrains.research.libsl2.ast.decl.AutomatonDecl) :
        Translator<AutomatonContext>(AutomatonContext(compat.globalCtx)) {

        private lateinit var automaton: Automaton

        fun translate() {
            val name = decl.name.typeName.toString()
            val typeRef = TypeTranslator(compat.globalCtx).translateTypeExpr(decl.typeExpr)
            val annotations = decl.annotations.mapTo(mutableListOf()) { translateAnnotation(it) }

            automaton = if (decl.isConcept) {
                Automaton(
                    isConcept = false,
                    name,
                    typeRef,
                    annotations,
                    context = ctx,
                    entityPosition = decl.location!!.toEntityPosition(),
                )
            } else {
                AutomatonConcept(
                    isConcept = true,
                    name,
                    typeRef,
                    annotations,
                    context = ctx,
                    entityPosition = decl.location!!.toEntityPosition(),
                )
            }

            decl.implementedConcepts.mapTo(automaton.implementedConcepts) { name ->
                ImplementedConcept(name.toString(), name.location!!.toEntityPosition())
            }

            for (varDecl in decl.constructorVariables) {
                val keyword = if (varDecl.mutable) VariableKind.VAR else VariableKind.VAL
                val name = varDecl.name.toString()
                val typeRef = TypeTranslator(compat.globalCtx).translateTypeExpr(varDecl.typeExpr)
                val init = varDecl.init?.let { ExprTranslator(ctx).translateExpr(it) }
                val node = ConstructorArgument(
                    keyword,
                    name,
                    typeRef,
                    varDecl.annotations.mapTo(mutableListOf()) { translateAnnotation(it) },
                    init,
                    varDecl.location!!.toEntityPosition(),
                )

                ctx.storeVariable(node)
                automaton.constructorVariables += node
            }

            for (decl in decl.decls) {
                when (decl) {
                    is org.jetbrains.research.libsl2.ast.decl.ConstructorDecl -> translateConstructorDecl(decl)
                    is org.jetbrains.research.libsl2.ast.decl.DestructorDecl -> translateDestructorDecl(decl)
                    is org.jetbrains.research.libsl2.ast.decl.FunctionDecl -> translateFunctionDecl(decl)
                    is org.jetbrains.research.libsl2.ast.decl.ProcDecl -> translateProcDecl(decl)
                    is org.jetbrains.research.libsl2.ast.decl.ShiftDecl -> translateShiftDecl(decl)
                    is org.jetbrains.research.libsl2.ast.decl.StateDecl -> translateStateDecl(decl)
                    is org.jetbrains.research.libsl2.ast.decl.VariableDecl -> translateVariableDecl(decl)
                }
            }

            compat.globalCtx.storeAutomata(automaton)
        }

        fun translateConstructorDecl(decl: org.jetbrains.research.libsl2.ast.decl.ConstructorDecl) {
            FunctionTranslator(decl, ctx, automaton).translate()
        }

        fun translateDestructorDecl(decl: org.jetbrains.research.libsl2.ast.decl.DestructorDecl) {
            FunctionTranslator(decl, ctx, automaton).translate()
        }

        fun translateFunctionDecl(decl: org.jetbrains.research.libsl2.ast.decl.FunctionDecl) {
            FunctionTranslator(decl, ctx, automaton).translate()
        }

        fun translateProcDecl(decl: org.jetbrains.research.libsl2.ast.decl.ProcDecl) {
            FunctionTranslator(decl, ctx, automaton).translate()
        }

        fun translateShiftDecl(decl: org.jetbrains.research.libsl2.ast.decl.ShiftDecl) {
            val toState = when (val name = decl.to.toString()) {
                "self" -> State(
                    name,
                    StateKind.SIMPLE,
                    isSelf = true,
                    entityPosition = decl.to.location!!.toEntityPosition(),
                )

                else -> automaton.states.firstOrNull { it.name == name } ?: run {
                    compat.addError(UnresolvedState("unresolved state: $name", decl.to.location!!.toEntityPosition()))
                    return
                }
            }

            for (state in decl.from) {
                val fromState = when (val name = state.toString()) {
                    "any" -> State(
                        name,
                        StateKind.SIMPLE,
                        isAny = true,
                        entityPosition = state.location!!.toEntityPosition(),
                    )

                    else -> automaton.states.firstOrNull { it.name == name } ?: run {
                        compat.addError(UnresolvedState("unresolved state: $name", state.location!!.toEntityPosition()))
                        continue
                    }
                }

                val funcRefs = decl.by.mapTo(mutableListOf()) { sig ->
                    val name = sig.name.toString()
                    val paramTypes = sig.params.orEmpty().mapTo(mutableListOf()) {
                        TypeTranslator(ctx).translateTypeExpr(it)
                    }

                    FunctionReferenceBuilder.build(name = name, argTypes = paramTypes, context = ctx)
                }

                val node = Shift(fromState, toState, funcRefs, decl.location!!.toEntityPosition())
                automaton.shifts += node
            }
        }

        fun translateStateDecl(decl: org.jetbrains.research.libsl2.ast.decl.StateDecl) {
            val kind = when (decl.kind) {
                org.jetbrains.research.libsl2.ast.decl.StateDecl.Kind.Initial -> StateKind.INIT
                org.jetbrains.research.libsl2.ast.decl.StateDecl.Kind.Regular -> StateKind.SIMPLE
                org.jetbrains.research.libsl2.ast.decl.StateDecl.Kind.Final -> StateKind.FINISH
            }

            val node = State(decl.name.toString(), kind, entityPosition = decl.location!!.toEntityPosition())

            automaton.states += node
        }

        fun translateVariableDecl(decl: org.jetbrains.research.libsl2.ast.decl.VariableDecl) {
            val keyword = if (decl.mutable) VariableKind.VAR else VariableKind.VAL
            val name = decl.name.toString()
            val typeRef = TypeTranslator(compat.globalCtx).translateTypeExpr(decl.typeExpr)
            val init = decl.init?.let { ExprTranslator(ctx).translateExpr(it) }
            val node = VariableWithInitialValue(
                keyword,
                name,
                typeRef,
                decl.annotations.mapTo(mutableListOf()) { translateAnnotation(it) },
                init,
                decl.location!!.toEntityPosition(),
            )

            automaton.internalVariables += node
            ctx.storeVariable(node)
        }
    }

    private inner class FunctionTranslator(
        val decl: org.jetbrains.research.libsl2.ast.decl.FunctionLikeDecl,
        parentCtx: LslContextBase,
        var automaton: Automaton?,
    ) : Translator<FunctionContext>(FunctionContext(parentCtx)) {
        private lateinit var function: Function

        fun translate() {
            when (decl) {
                is org.jetbrains.research.libsl2.ast.decl.ConstructorDecl -> translate(decl)
                is org.jetbrains.research.libsl2.ast.decl.DestructorDecl -> translate(decl)
                is org.jetbrains.research.libsl2.ast.decl.FunctionDecl -> translate(decl)
                is org.jetbrains.research.libsl2.ast.decl.ProcDecl -> translate(decl)
            }
        }

        private fun translate(decl: org.jetbrains.research.libsl2.ast.decl.ConstructorDecl) {
            val name = decl.name.toString()
            val annotations = decl.annotations.mapTo(mutableListOf()) { translateAnnotation(it) }
            val params = translateParams(decl.params)
            params.forEach { ctx.storeFunctionArgument(it) }

            function = Constructor(
                name,
                params,
                annotations,
                hasBody = decl.body != null,
                context = ctx,
                isMethod = decl.isMethod,
                entityPosition = decl.location!!.toEntityPosition(),
            )

            decl.body?.let(::translateBody)
            automaton?.constructors?.add(function)
        }

        private fun translate(decl: org.jetbrains.research.libsl2.ast.decl.DestructorDecl) {
            val name = decl.name.toString()
            val annotations = decl.annotations.mapTo(mutableListOf()) { translateAnnotation(it) }
            val params = translateParams(decl.params)
            params.forEach { ctx.storeFunctionArgument(it) }

            function = Destructor(
                name,
                params,
                annotations,
                hasBody = decl.body != null,
                context = ctx,
                isMethod = decl.isMethod,
                entityPosition = decl.location!!.toEntityPosition(),
            )

            decl.body?.let(::translateBody)
            automaton?.destructors?.add(function)
        }

        private fun translate(decl: org.jetbrains.research.libsl2.ast.decl.FunctionDecl) {
            val automatonName = decl.extensionFor?.toString()
            val automatonRef = automatonName?.let { AutomatonReferenceBuilder.build(it, ctx) }
                ?: automaton?.getReference(ctx)

            if (automatonName != null) {
                automaton = automatonRef?.resolveOrError()
            }

            val name = decl.name.toString()
            val annotations = decl.annotations.mapTo(mutableListOf()) { translateAnnotation(it) }
            val params = translateParams(decl.params)
            params.forEach { ctx.storeFunctionArgument(it) }

            val targetAutomatonRef = params
                .firstOrNull { arg ->
                    arg.annotationUsages.any { it.annotationReference.name == "target" }
                }
                ?.typeReference
                ?.toSimpleString()
                ?.let { AutomatonReferenceBuilder.build(it, ctx) }
                ?: automatonRef

            val returnType = decl.returnType?.let { TypeTranslator(ctx).translateTypeExpr(it) }
            val generics = translateGenerics(decl.generics, decl.typeConstraints)
            generics.forEach { ctx.storeFunctionType(it) }

            if (returnType != null) {
                val resultVar = ResultVariable(
                    returnType,
                    decl.returnType!!.location!!.toEntityPosition(),
                )
                ctx.storeVariable(resultVar)
            }

            function = Function(
                kind = FunctionKind.FUNCTION,
                name,
                automatonRef,
                params,
                returnType,
                annotations,
                hasBody = decl.body != null,
                targetAutomatonRef = targetAutomatonRef,
                context = ctx,
                isMethod = decl.isMethod,
                isStatic = decl.isStatic,
                entityPosition = decl.location!!.toEntityPosition(),
            )

            decl.body?.let(::translateBody)
            automaton?.localFunctions?.add(function)
        }

        private fun translate(decl: org.jetbrains.research.libsl2.ast.decl.ProcDecl) {
            val name = decl.name.toString()
            val annotations = decl.annotations.mapTo(mutableListOf()) { translateAnnotation(it) }
            val params = translateParams(decl.params)
            val generics = translateGenerics(decl.generics, decl.typeConstraints)

            generics.forEach { ctx.storeFunctionType(it) }
            params.forEach { ctx.storeFunctionArgument(it) }

            val returnType = decl.returnType?.let { TypeTranslator(ctx).translateTypeExpr(it) }

            if (returnType != null) {
                val resultVar = ResultVariable(
                    returnType,
                    decl.returnType!!.location!!.toEntityPosition(),
                )
                ctx.storeVariable(resultVar)
            }

            function = Procedure(
                name,
                params,
                returnType,
                annotations,
                hasBody = decl.body != null,
                context = ctx,
                isMethod = decl.isMethod,
                entityPosition = decl.location!!.toEntityPosition(),
            )

            decl.body?.let(::translateBody)
            compat.globalCtx.storeFunction(function)
            automaton?.procDeclarations?.add(function)
        }

        private fun translateParams(params: List<org.jetbrains.research.libsl2.ast.FunctionParam>): MutableList<FunctionArgument> =
            params.mapIndexedTo(mutableListOf()) { idx, param ->
                val typeRef = TypeTranslator(ctx).translateTypeExpr(param.typeExpr)
                val annotations = param.annotations.mapTo(mutableListOf()) { translateAnnotation(it) }

                FunctionArgument(
                    param.name.toString(),
                    typeRef,
                    idx,
                    annotations,
                    null,
                    param.name.location!!.toEntityPosition(),
                )
            }

        private fun translateBody(body: org.jetbrains.research.libsl2.ast.FunctionBody) {
            for (contract in body.contracts) {
                when (contract) {
                    is org.jetbrains.research.libsl2.ast.contract.AssignsContract -> translateContract(contract)
                    is org.jetbrains.research.libsl2.ast.contract.EnsuresContract -> translateContract(contract)
                    is org.jetbrains.research.libsl2.ast.contract.RequiresContract -> translateContract(contract)
                }
            }

            for (stmt in body.stmts) {
                translateStmt(stmt, function.statements)
            }
        }

        private fun translateContract(contract: org.jetbrains.research.libsl2.ast.contract.AssignsContract) {
            function.contracts.add(
                Contract(
                    contract.name?.toString(),
                    ExprTranslator(ctx).translateExpr(contract.expr),
                    ContractKind.ASSIGNS,
                    contract.location!!.toEntityPosition(),
                ),
            )
        }

        private fun translateContract(contract: org.jetbrains.research.libsl2.ast.contract.EnsuresContract) {
            function.contracts.add(
                Contract(
                    contract.name?.toString(),
                    ExprTranslator(ctx).translateExpr(contract.expr),
                    ContractKind.ENSURES,
                    contract.location!!.toEntityPosition(),
                ),
            )
        }

        private fun translateContract(contract: org.jetbrains.research.libsl2.ast.contract.RequiresContract) {
            function.contracts.add(
                Contract(
                    contract.name?.toString(),
                    ExprTranslator(ctx).translateExpr(contract.expr),
                    ContractKind.REQUIRES,
                    contract.location!!.toEntityPosition(),
                ),
            )
        }

        private fun translateStmt(
            stmt: org.jetbrains.research.libsl2.ast.stmt.Stmt,
            statements: MutableList<Statement>,
        ) {
            if (automaton is AutomatonConcept) {
                error("Function realisation inside automaton concept")
            }

            when (stmt) {
                is org.jetbrains.research.libsl2.ast.stmt.AssignStmt -> translateStmt(stmt, statements)
                is org.jetbrains.research.libsl2.ast.stmt.ExprStmt -> translateStmt(stmt, statements)
                is org.jetbrains.research.libsl2.ast.stmt.IfStmt -> translateStmt(stmt, statements)
                is org.jetbrains.research.libsl2.ast.stmt.VariableDeclStmt -> translateStmt(stmt, statements)
            }
        }

        private fun translateStmt(
            stmt: org.jetbrains.research.libsl2.ast.stmt.AssignStmt,
            statements: MutableList<Statement>,
        ) {
            val assignee = ExprTranslator(ctx).translateAccess(stmt.lhs)

            val op = when (stmt.inPlaceOp) {
                null -> AssignOps.ASSIGN
                org.jetbrains.research.libsl2.ast.stmt.AssignStmt.InPlaceOp.Add -> AssignOps.COMP_ADD
                org.jetbrains.research.libsl2.ast.stmt.AssignStmt.InPlaceOp.Sub -> AssignOps.COMP_SUB
                org.jetbrains.research.libsl2.ast.stmt.AssignStmt.InPlaceOp.Mul -> AssignOps.COMP_MUL
                org.jetbrains.research.libsl2.ast.stmt.AssignStmt.InPlaceOp.Div -> AssignOps.COMP_DIV
                org.jetbrains.research.libsl2.ast.stmt.AssignStmt.InPlaceOp.Mod -> AssignOps.COMP_MOD
                org.jetbrains.research.libsl2.ast.stmt.AssignStmt.InPlaceOp.BitAnd -> AssignOps.COMP_AND
                org.jetbrains.research.libsl2.ast.stmt.AssignStmt.InPlaceOp.BitOr -> AssignOps.COMP_OR
                org.jetbrains.research.libsl2.ast.stmt.AssignStmt.InPlaceOp.BitXor -> AssignOps.COMP_XOR
                org.jetbrains.research.libsl2.ast.stmt.AssignStmt.InPlaceOp.LShift -> AssignOps.COMP_L_SHIFT
                org.jetbrains.research.libsl2.ast.stmt.AssignStmt.InPlaceOp.RShift -> AssignOps.COMP_R_SHIFT
            }

            val expr = ExprTranslator(ctx).translateExpr(stmt.rhs)

            statements += Assignment(assignee, op, expr, stmt.location!!.toEntityPosition())
        }

        private fun translateStmt(
            stmt: org.jetbrains.research.libsl2.ast.stmt.ExprStmt,
            statements: MutableList<Statement>,
        ) {
            val expr = ExprTranslator(ctx).translateExpr(stmt.expr)

            statements += ExpressionStatement(expr, stmt.location!!.toEntityPosition())
        }

        private fun translateStmt(
            stmt: org.jetbrains.research.libsl2.ast.stmt.IfStmt,
            statements: MutableList<Statement>,
        ) {
            val condition = ExprTranslator(ctx).translateExpr(stmt.condition)
            val thenBranch = mutableListOf<Statement>()

            for (stmt in stmt.thenBranch) {
                translateStmt(stmt, thenBranch)
            }

            val elseBranch = stmt.elseBranch?.let { block ->
                val elseBranch = mutableListOf<Statement>()

                for (stmt in block) {
                    translateStmt(stmt, elseBranch)
                }

                ElseStatement(elseBranch, stmt.location!!.toEntityPosition())
            }

            statements += IfStatement(condition, thenBranch, elseBranch, stmt.location!!.toEntityPosition())
        }

        private fun translateStmt(
            stmt: org.jetbrains.research.libsl2.ast.stmt.VariableDeclStmt,
            statements: MutableList<Statement>,
        ) {
            val keyword = if (stmt.decl.mutable) VariableKind.VAR else VariableKind.VAL
            val name = stmt.decl.name.toString()
            val typeRef = TypeTranslator(compat.globalCtx).translateTypeExpr(stmt.decl.typeExpr)
            val init = stmt.decl.init?.let { ExprTranslator(compat.globalCtx).translateExpr(it) }
            val annotations = stmt.decl.annotations.mapTo(mutableListOf(), ::translateAnnotation)
            val variable = VariableWithInitialValue(
                keyword,
                name,
                typeRef,
                annotations,
                init,
                stmt.decl.location!!.toEntityPosition(),
            )

            statements += VariableDeclaration(variable, stmt.location!!.toEntityPosition())
            ctx.storeVariable(variable)
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

        fun translateAccess(access: org.jetbrains.research.libsl2.ast.access.Access): QualifiedAccess {
            TODO()
        }
    }
}
