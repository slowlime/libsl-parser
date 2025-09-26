package org.jetbrains.research.libsl.resolve

import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.ast.FunctionBody
import org.jetbrains.research.libsl.ast.FunctionParam
import org.jetbrains.research.libsl.ast.Generic
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.TypeConstraint
import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.ast.access.NameAccess
import org.jetbrains.research.libsl.ast.decl.ActionDecl
import org.jetbrains.research.libsl.ast.decl.AnnotationDecl
import org.jetbrains.research.libsl.ast.decl.AutomatonDecl
import org.jetbrains.research.libsl.ast.decl.ConstructorDecl
import org.jetbrains.research.libsl.ast.decl.DestructorDecl
import org.jetbrains.research.libsl.ast.decl.EnumDecl
import org.jetbrains.research.libsl.ast.decl.FunctionDecl
import org.jetbrains.research.libsl.ast.decl.ImportDecl
import org.jetbrains.research.libsl.ast.decl.IncludeDecl
import org.jetbrains.research.libsl.ast.decl.ProcDecl
import org.jetbrains.research.libsl.ast.decl.SemanticTypeDecl
import org.jetbrains.research.libsl.ast.decl.ShiftDecl
import org.jetbrains.research.libsl.ast.decl.StateDecl
import org.jetbrains.research.libsl.ast.decl.StructDecl
import org.jetbrains.research.libsl.ast.decl.TypeAliasDecl
import org.jetbrains.research.libsl.ast.decl.VariableDecl
import org.jetbrains.research.libsl.ast.expr.ActionCallExpr
import org.jetbrains.research.libsl.ast.expr.InstantiationExpr
import org.jetbrains.research.libsl.ast.expr.ProcCallExpr
import org.jetbrains.research.libsl.ast.stmt.IfStmt
import org.jetbrains.research.libsl.ast.type.NameTypeExpr
import org.jetbrains.research.libsl.ast.walk
import org.jetbrains.research.libsl.exception.ConflictingDefinitionException
import org.jetbrains.research.libsl.exception.ConflictingImportException
import org.jetbrains.research.libsl.exception.ConflictingParamNameException
import org.jetbrains.research.libsl.exception.TooFewArgumentsException
import org.jetbrains.research.libsl.exception.TooManyArgumentsException
import org.jetbrains.research.libsl.exception.UnorderedMixedNamedAndUnnamedArgumentsException
import org.jetbrains.research.libsl.exception.UnresolvedReferenceException
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.scope.ModuleScope
import org.jetbrains.research.libsl.resolve.scope.MutableScope
import org.jetbrains.research.libsl.resolve.scope.Scope
import org.jetbrains.research.libsl.type.TypeConstructor
import org.jetbrains.research.libsl.type.TypeParam

internal class NameResolver(private val libsl: LibSL, private val rootModule: Module) {
    // populated in `addTopLevelDefs`; in reverse post-order
    private val modules = mutableListOf<Module>()

    fun resolve() {
        addTopLevelDefs()
        addImportedEntities()
        resolveTopLevelDefs()
    }

    private fun <T> MutableScope.DefinitionResult<T>.orThrow(name: Name): Def.Primary<T> =
        orThrow(name.toString(), name.location)

    private fun <T> MutableScope.DefinitionResult<T>.orThrow(name: String, location: Location?): Def.Primary<T> =
        when (this) {
            is MutableScope.DefinitionResult.Success -> this.def

            is MutableScope.DefinitionResult.Conflict -> throw ConflictingDefinitionException.fromName(
                name,
                location,
                this.previousDef.location,
            )
        }

    private fun <T> MutableScope.DefinitionResult<T>.orThrowParamConflict(name: Name): Def.Primary<T> =
        orThrowParamConflict(name.toString(), name.location)

    private fun <T> MutableScope.DefinitionResult<T>.orThrowParamConflict(
        name: String,
        location: Location?,
    ): Def.Primary<T> = when (this) {
        is MutableScope.DefinitionResult.Success -> this.def

        is MutableScope.DefinitionResult.Conflict -> throw ConflictingParamNameException.fromName(
            name,
            location,
            this.previousDef.location,
        )
    }

    private fun addTopLevelDefs() {
        data class Task(val module: Module, var declIdx: Int = 0)

        val discoveredModules = mutableSetOf(rootModule)
        val taskStack = mutableListOf(Task(rootModule))

        dfs@ while (taskStack.isNotEmpty()) {
            val task = taskStack.last()
            val module = task.module

            while (task.declIdx < module.decls.size) {
                when (val decl = module.decls[task.declIdx++]) {
                    is ActionDecl -> {
                        decl.primaryDef =
                            module.scope.define(decl.name.toString(), decl.name.location, decl).orThrow(decl.name)
                    }

                    is AnnotationDecl -> {
                        decl.primaryDef =
                            module.scope.define(decl.name.toString(), decl.name.location, decl).orThrow(decl.name)
                    }

                    is AutomatonDecl -> {
                        val name = decl.name.typeName.toString()
                        val location = decl.name.typeName.location

                        decl.primaryDef = module.scope.define(name, location, decl).orThrow(name, location)
                    }

                    is EnumDecl -> {
                        val name = decl.typeName.typeName.toString()
                        val location = decl.typeName.typeName.location

                        decl.primaryDef = module.scope
                            .define(name, location, TypeConstructor.Enum(decl))
                            .orThrow(name, location)
                    }

                    is FunctionDecl -> {
                        decl.primaryDef =
                            module.scope.define(decl.name.toString(), decl.location, decl).orThrow(decl.name)
                    }

                    is ImportDecl -> {
                        if (discoveredModules.add(decl.importedModule)) {
                            taskStack += Task(decl.importedModule)
                            module.imports += decl
                            decl.importedModule.importedBy += Pair(module, decl)

                            // process children first
                            continue@dfs
                        }
                    }

                    is IncludeDecl -> {}

                    is SemanticTypeDecl -> {
                        val name = decl.typeName.typeName.toString()
                        val location = decl.typeName.typeName.location

                        decl.primaryDef = module.scope
                            .define(name, location, TypeConstructor.Semantic(decl))
                            .orThrow(name, location)
                    }

                    is StructDecl -> {
                        val name = decl.typeName.typeName.toString()
                        val location = decl.typeName.typeName.location

                        decl.primaryDef = module.scope
                            .define(name, location, TypeConstructor.Struct(decl))
                            .orThrow(name, location)
                    }

                    is TypeAliasDecl -> {
                        val name = decl.typeName.typeName.toString()
                        val location = decl.typeName.typeName.location

                        decl.primaryDef = module.scope
                            .define(name, location, TypeConstructor.Alias(decl))
                            .orThrow(name, location)
                    }

                    is VariableDecl -> {
                        decl.primaryDef = module.scope
                            .define(decl.name.toString(), decl.name.location, Binding.of(decl))
                            .orThrow(decl.name)
                    }
                }
            }

            modules += module
            taskStack.removeLast()
        }
    }

    private fun addImportedEntities() {
        val stack = modules.asReversed().toMutableList()
        val queued = modules.toMutableSet()

        fun push(module: Module) {
            if (queued.add(module)) {
                stack += module
            }
        }

        while (stack.isNotEmpty()) {
            val module = stack.removeLast()
            queued -= module

            for (decl in module.imports) {
                val importedModule = decl.importedModule

                fun <T> handle(result: ModuleScope.ImportResult<T>, def: Def<T>) {
                    when (result) {
                        is ModuleScope.ImportResult.Success if result.new -> {
                            for ((dependent, _) in module.importedBy) {
                                push(dependent)
                            }
                        }

                        is ModuleScope.ImportResult.Success -> {}

                        is ModuleScope.ImportResult.Conflict -> throw ConflictingImportException.fromName(
                            def.name,
                            decl.location,
                            def.primary.location,
                            result.previousDef.location,
                            result.previousDef.primary.location,
                        )
                    }
                }

                for (def in importedModule.scope.allTypes) {
                    handle(module.scope.importType(decl.location, def), def)
                }

                for (def in importedModule.scope.allAutomata) {
                    handle(module.scope.importAutomaton(decl.location, def), def)
                }

                for (def in importedModule.scope.allFunctions) {
                    handle(module.scope.importFunction(decl.location, def), def)
                }

                for (def in importedModule.scope.allBindings) {
                    handle(module.scope.importBinding(decl.location, def), def)
                }

                for (def in importedModule.scope.allAnnotations) {
                    handle(module.scope.importAnnotation(decl.location, def), def)
                }

                for (def in importedModule.scope.allActions) {
                    handle(module.scope.importAction(decl.location, def), def)
                }
            }
        }
    }

    private fun resolveTopLevelDefs() {
        for (module in modules.asReversed()) {
            for (decl in module.decls) {
                DefVisitor(module).visit(decl)
            }
        }
    }

    private inner class DefVisitor(module: Module) : Visitor() {
        private var currentScope: MutableScope = module.scope

        private inline fun <T : MutableScope> enter(scope: T, f: () -> Unit): T {
            val previousScope = currentScope
            currentScope = scope

            try {
                f()
            } finally {
                currentScope = previousScope
            }

            return scope
        }

        private fun defineGeneric(generic: Generic): TypeParam {
            val param = TypeParam(generic)

            generic.primaryDef = currentScope
                .define(generic.name.toString(), generic.name.location, TypeConstructor.Nullary(param))
                .orThrowParamConflict(generic.name)

            return param
        }

        private fun defineParam(param: FunctionParam) {
            param.primaryDef = currentScope
                .define(param.name.toString(), param.name.location, Binding.of(param))
                .orThrowParamConflict(param.name)
        }

        private fun defineMembers(decl: AutomatonDecl) {
            for (varDecl in decl.constructorVariables) {
                varDecl.primaryDef = currentScope
                    .define(varDecl.name.toString(), varDecl.location, Binding.of(varDecl))
                    .orThrow(varDecl.name)
            }

            for (decl in decl.decls) {
                when (decl) {
                    is ConstructorDecl -> {
                        decl.primaryDef = currentScope
                            .define(decl.name.toString(), decl.name.location, decl)
                            .orThrow(decl.name)
                    }

                    is DestructorDecl -> {
                        decl.primaryDef = currentScope
                            .define(decl.name.toString(), decl.name.location, decl)
                            .orThrow(decl.name)
                    }

                    is FunctionDecl -> {
                        decl.primaryDef = currentScope
                            .define(decl.name.toString(), decl.name.location, decl)
                            .orThrow(decl.name)
                    }

                    is ProcDecl -> {
                        decl.primaryDef = currentScope
                            .define(decl.name.toString(), decl.name.location, decl)
                            .orThrow(decl.name)
                    }

                    is ShiftDecl -> {}

                    is StateDecl -> {
                        decl.primaryDef = currentScope
                            .define(decl.name.toString(), decl.name.location, decl)
                            .orThrow(decl.name)
                    }

                    is VariableDecl -> {
                        decl.primaryDef = currentScope
                            .define(decl.name.toString(), decl.name.location, Binding.of(decl))
                            .orThrow(decl.name)
                    }
                }
            }
        }

        private fun defineMembers(decl: StructDecl) {
            for (decl in decl.decls) {
                when (decl) {
                    is FunctionDecl -> {
                        decl.primaryDef = currentScope
                            .define(decl.name.toString(), decl.name.location, decl)
                            .orThrow(decl.name)
                    }

                    is ProcDecl -> {
                        decl.primaryDef = currentScope
                            .define(decl.name.toString(), decl.name.location, decl)
                            .orThrow(decl.name)
                    }

                    is VariableDecl -> {
                        decl.primaryDef = currentScope
                            .define(decl.name.toString(), decl.name.location, Binding.of(decl))
                            .orThrow(decl.name)
                    }
                }
            }
        }

        private fun visit(typeConstraint: TypeConstraint, paramScope: Scope) {
            typeConstraint.resolvedParam = paramScope.resolveTypeLocally(typeConstraint.param.toString())
                ?: throw UnresolvedReferenceException.toTypeParamInConstraint(
                    typeConstraint.param.toString(),
                    typeConstraint.param.location,
                )

            visit(typeConstraint.bound)
        }

        private fun visit(body: FunctionBody, paramScope: Scope) {
            body.scope = enter(MutableScope(currentScope, resolutionParent = paramScope)) {
                for (contract in body.contracts) {
                    visit(contract)
                }

                for (stmt in body.stmts) {
                    visit(stmt)
                }
            }
        }

        override fun visit(decl: ActionDecl) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.paramScope = enter(MutableScope(currentScope)) {
                for (generic in decl.generics) {
                    defineGeneric(generic)
                }

                for (param in decl.params) {
                    param.primaryDef = currentScope
                        .define(param.name.toString(), param.name.location, Binding.of(param))
                        .orThrowParamConflict(param.name)
                }

                decl.returnType?.let(this::visit)

                for (typeConstraint in decl.typeConstraints) {
                    visit(typeConstraint, currentScope)
                }
            }
        }

        override fun visit(decl: AnnotationDecl) {
            decl.paramScope = enter(MutableScope(currentScope)) {
                for (param in decl.params) {
                    param.primaryDef = currentScope
                        .define(param.name.toString(), param.name.location, Binding.of(param))
                        .orThrowParamConflict(param.name)
                }
            }
        }

        override fun visit(decl: AutomatonDecl) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.scope = enter(MutableScope(currentScope)) {
                defineMembers(decl)

                for (generic in decl.name.generics) {
                    defineGeneric(generic)
                }

                for (varDecl in decl.constructorVariables) {
                    visit(varDecl)
                }

                visit(decl.typeExpr)

                for (decl in decl.decls) {
                    visit(decl)
                }
            }
        }

        override fun visit(decl: EnumDecl) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.scope = enter(MutableScope(currentScope)) {
                val typeParams = (decl.primaryDef.entity as TypeConstructor.Enum).params

                for (generic in decl.typeName.generics) {
                    typeParams += defineGeneric(generic)
                }

                for (variant in decl.variants) {
                    variant.primaryDef = currentScope
                        .define(variant.name.toString(), variant.name.location, Binding.of(variant))
                        .orThrow(variant.name)
                }
            }
        }

        override fun visit(decl: FunctionDecl) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.resolvedExtensionFor = decl.extensionFor?.let { fullName ->
                currentScope.resolveAutomaton(fullName.toString())
                    ?: throw UnresolvedReferenceException.toAutomaton(fullName.toString(), fullName.location)
            }

            decl.paramScope = enter(MutableScope(currentScope)) {
                for (generic in decl.generics) {
                    defineGeneric(generic)
                }

                for (param in decl.params) {
                    defineParam(param)
                }

                decl.returnType?.let(this::visit)

                for (typeConstraint in decl.typeConstraints) {
                    visit(typeConstraint, currentScope)
                }
            }

            decl.body?.let { visit(it, decl.paramScope) }
        }

        override fun visit(decl: SemanticTypeDecl.Simple) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.scope = enter(MutableScope(currentScope)) {
                for (generic in decl.typeName.generics) {
                    defineGeneric(generic)
                }

                visit(decl.realType)
            }
        }

        override fun visit(decl: SemanticTypeDecl.Enumerated) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.scope = enter(MutableScope(currentScope)) {
                val typeParams = (decl.primaryDef.entity as TypeConstructor.Semantic).params

                for (generic in decl.typeName.generics) {
                    typeParams += defineGeneric(generic)
                }

                visit(decl.realType)

                for (value in decl.values) {
                    value.primaryDef = currentScope
                        .define(value.name.toString(), value.name.location, Binding.of(value))
                        .orThrow(value.name)
                }
            }
        }

        override fun visit(decl: StructDecl) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.scope = enter(MutableScope(currentScope)) {
                defineMembers(decl)

                val typeParams = (decl.primaryDef.entity as TypeConstructor.Semantic).params

                for (generic in decl.typeName.generics) {
                    typeParams += defineGeneric(generic)
                }

                decl.isType?.let(this::visit)

                for (typeExpr in decl.forTypes) {
                    visit(typeExpr)
                }

                for (typeConstraint in decl.typeConstraints) {
                    visit(typeConstraint, currentScope)
                }

                for (decl in decl.decls) {
                    visit(decl)
                }
            }
        }

        override fun visit(decl: TypeAliasDecl) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.scope = enter(MutableScope(currentScope)) {
                val typeParams = (decl.primaryDef.entity as TypeConstructor.Alias).params

                for (generic in decl.typeName.generics) {
                    typeParams += defineGeneric(generic)
                }

                visit(decl.typeExpr)
            }
        }

        override fun visit(decl: VariableDecl) {
            if (!decl.primaryDefInitialized) {
                decl.primaryDef = currentScope
                    .define(decl.name.toString(), decl.name.location, Binding.of(decl))
                    .orThrow(decl.name)
            }

            super.visit(decl)
        }

        override fun visit(decl: ShiftDecl) {
            fun resolveState(state: Name): Def<StateDecl> =
                currentScope.resolveState(state.toString())
                    ?: throw UnresolvedReferenceException.toState(state.toString(), state.location)

            decl.fromStates = decl.from.asSequence().map { resolveState(it) }.toMutableList()
            decl.toState = resolveState(decl.to)

            for (sig in decl.by) {
                sig.walk(this)
            }
        }

        override fun visit(decl: ConstructorDecl) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.paramScope = enter(MutableScope(currentScope)) {
                for (param in decl.params) {
                    defineParam(param)
                }

                decl.returnType?.let(this::visit)
            }

            decl.body?.let { visit(it, decl.paramScope) }
        }

        override fun visit(decl: DestructorDecl) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.paramScope = enter(MutableScope(currentScope)) {
                for (param in decl.params) {
                    defineParam(param)
                }

                decl.returnType?.let(this::visit)
            }

            decl.body?.let { visit(it, decl.paramScope) }
        }

        override fun visit(decl: ProcDecl) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.paramScope = enter(MutableScope(currentScope)) {
                for (generic in decl.generics) {
                    defineGeneric(generic)
                }

                for (param in decl.params) {
                    defineParam(param)
                }

                decl.returnType?.let(this::visit)

                for (typeConstraint in decl.typeConstraints) {
                    visit(typeConstraint, currentScope)
                }
            }

            decl.body?.let { visit(it, decl.paramScope) }
        }

        override fun visit(annotation: LibSLAnnotation) {
            annotation.resolved = currentScope.resolveAnnotation(annotation.name.toString())
                ?: throw UnresolvedReferenceException.toAnnotation(annotation.name.toString(), annotation.name.location)

            val params = annotation.resolved.entity.params
            val args = annotation.args

            when {
                args.size > params.size -> throw TooManyArgumentsException(
                    args[params.size].expr.location,
                    args.size,
                    params.size,
                )

                args.size < params.size -> throw TooFewArgumentsException(
                    annotation.location,
                    args.size,
                    params.size,
                )
            }

            var argsInOrder = true

            for ((idx, arg) in args.withIndex()) {
                val name = arg.name

                arg.paramIndex = if (name == null) {
                    if (!argsInOrder) {
                        throw UnorderedMixedNamedAndUnnamedArgumentsException(arg.expr.location)
                    }

                    idx
                } else {
                    when (val idx = params.indexOfFirst { it.name.toString() == name.toString() }) {
                        -1 -> throw UnresolvedReferenceException.toParam(name.toString(), name.location)
                        else -> idx
                    }
                }

                if (arg.paramIndex != idx) {
                    argsInOrder = false
                }
            }

            for (arg in args) {
                visit(arg.expr)
            }
        }

        override fun visit(typeExpr: NameTypeExpr) {
            typeExpr.resolvedTypeName = currentScope.resolveType(typeExpr.typeName.toString())
                ?: throw UnresolvedReferenceException.toType(typeExpr.typeName.toString(), typeExpr.typeName.location)

            super.visit(typeExpr)
        }

        override fun visit(stmt: IfStmt) {
            visit(stmt.condition)

            stmt.thenScope = enter(MutableScope(currentScope)) {
                for (stmt in stmt.thenBranch) {
                    visit(stmt)
                }
            }

            stmt.elseScope = enter(MutableScope(currentScope)) {
                stmt.elseBranch?.let { elseBranch ->
                    for (stmt in elseBranch) {
                        visit(stmt)
                    }
                }
            }
        }

        override fun visit(expr: ActionCallExpr) {
            expr.resolved = currentScope.resolveAction(expr.name.toString())
                ?: throw UnresolvedReferenceException.toAction(expr.name.toString(), expr.name.location)

            super.visit(expr)
        }

        override fun visit(expr: InstantiationExpr) {
            expr.resolved = currentScope.resolveAutomaton(expr.name.toString())
                ?: throw UnresolvedReferenceException.toAutomaton(expr.name.toString(), expr.name.location)

            super.visit(expr)
        }

        override fun visit(expr: ProcCallExpr) {
            // don't resolve the callee here: we need typing information for that
            // visit(expr.callee)

            expr.typeArgs?.forEach { visit(it) }

            for (arg in expr.args) {
                visit(arg)
            }
        }

        // assumes the access refers to a binding.
        override fun visit(access: NameAccess) {
            access.resolved = currentScope.resolveBinding(access.name.toString())
                ?: throw UnresolvedReferenceException.toBinding(access.name.toString(), access.name.location)
        }
    }
}
