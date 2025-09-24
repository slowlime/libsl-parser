package org.jetbrains.research.libsl.resolve

import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.ast.FunctionBody
import org.jetbrains.research.libsl.ast.FunctionParam
import org.jetbrains.research.libsl.ast.Generic
import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.TypeConstraint
import org.jetbrains.research.libsl.ast.Visitor
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
import org.jetbrains.research.libsl.ast.walk
import org.jetbrains.research.libsl.exception.ConflictingDefinitionException
import org.jetbrains.research.libsl.exception.ConflictingImportException
import org.jetbrains.research.libsl.exception.ConflictingParamNameException
import org.jetbrains.research.libsl.exception.UnresolvedReference
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.scope.ModuleScope
import org.jetbrains.research.libsl.resolve.scope.MutableScope
import org.jetbrains.research.libsl.resolve.scope.Scope
import org.jetbrains.research.libsl.type.AliasType
import org.jetbrains.research.libsl.type.EnumType
import org.jetbrains.research.libsl.type.SemanticType
import org.jetbrains.research.libsl.type.StructType
import org.jetbrains.research.libsl.type.TypeParam

internal class ModuleResolver(private val libsl: LibSL, private val rootModule: Module) {
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
                        // TODO: define members too.
                        val name = decl.name.typeName.toString()
                        val location = decl.name.typeName.location

                        decl.primaryDef = module.scope.define(name, location, decl).orThrow(name, location)
                    }

                    is EnumDecl -> {
                        val name = decl.typeName.typeName.toString()
                        val location = decl.typeName.typeName.location

                        decl.primaryDef = module.scope.define(name, location, EnumType(decl)).orThrow(name, location)
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

                        decl.primaryDef = module.scope.define(name, location, SemanticType(decl))
                            .orThrow(name, location)
                    }

                    is StructDecl -> {
                        // TODO: define members too.
                        val name = decl.typeName.typeName.toString()
                        val location = decl.typeName.typeName.location

                        decl.primaryDef = module.scope.define(name, location, StructType(decl))
                            .orThrow(name, location)
                    }

                    is TypeAliasDecl -> {
                        val name = decl.typeName.typeName.toString()
                        val location = decl.typeName.typeName.location

                        decl.primaryDef = module.scope.define(name, location, AliasType(decl)).orThrow(name, location)
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

                for ((_, def) in importedModule.scope.types) {
                    when (val result = module.scope.importType(decl.location, def)) {
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
            }
        }
    }

    private fun resolveTopLevelDefs() {
        for (module in modules.asReversed()) {
            for (decl in module.decls) {
                TopLevelDefVisitor(module).visit(decl)
            }
        }
    }

    private inner class TopLevelDefVisitor(module: Module) : Visitor() {
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

        private fun defineGeneric(generic: Generic) {
            generic.primaryDef = currentScope
                .define(generic.name.toString(), generic.name.location, TypeParam(generic))
                .orThrowParamConflict(generic.name)
        }

        private fun defineParam(param: FunctionParam) {
            param.primaryDef = currentScope
                .define(param.name.toString(), param.name.location, Binding.of(param))
                .orThrowParamConflict(param.name)
        }

        private fun visit(typeConstraint: TypeConstraint, paramScope: Scope) {
            typeConstraint.resolvedParam = paramScope.resolveTypeLocally(typeConstraint.param.toString())
                ?: throw UnresolvedReference.toTypeParamInConstraint(
                    typeConstraint.param.toString(),
                    typeConstraint.param.location,
                )

            visit(typeConstraint.bound)
        }

        private fun visit(body: FunctionBody, paramScope: Scope) {
            TODO()
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

            // TODO: resolve function references in state transition declarations.
        }

        override fun visit(decl: EnumDecl) {
            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.scope = enter(MutableScope(currentScope)) {
                for (generic in decl.typeName.generics) {
                    defineGeneric(generic)
                }

                for (variant in decl.variants) {
                    variant.primaryDef = currentScope
                        .define(variant.name.toString(), variant.name.location, Binding.of(variant))
                        .orThrow(variant.name)
                }
            }
        }

        override fun visit(decl: FunctionDecl) {
            if (!decl.primaryDefInitialized) {
                decl.primaryDef = currentScope
                    .define(decl.name.toString(), decl.name.location, decl)
                    .orThrow(decl.name)
            }

            for (annotation in decl.annotations) {
                visit(annotation)
            }

            decl.resolvedExtensionFor = decl.extensionFor?.let { fullName ->
                currentScope.resolveAutomaton(fullName.toString())
                    ?: throw UnresolvedReference.toAutomaton(fullName.toString(), fullName.location)
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
                for (generic in decl.typeName.generics) {
                    defineGeneric(generic)
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
                for (generic in decl.typeName.generics) {
                    defineGeneric(generic)
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
                for (generic in decl.typeName.generics) {
                    defineGeneric(generic)
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
                    ?: throw UnresolvedReference.toState(state.toString(), state.location)

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
    }
}
