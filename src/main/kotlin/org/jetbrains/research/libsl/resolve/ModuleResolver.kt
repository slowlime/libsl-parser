package org.jetbrains.research.libsl.resolve

import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.ast.Name
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
import org.jetbrains.research.libsl.exception.ConflictingDefinitionException
import org.jetbrains.research.libsl.exception.ConflictingImportException
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.scope.ModuleScope
import org.jetbrains.research.libsl.resolve.scope.MutableScope
import org.jetbrains.research.libsl.type.AliasType
import org.jetbrains.research.libsl.type.EnumType
import org.jetbrains.research.libsl.type.SemanticType
import org.jetbrains.research.libsl.type.StructType

internal class ModuleResolver(private val libsl: LibSL, private val rootModule: Module) {
    // populated in `addTopLevelDefs`; in reverse post-order
    private val modules = mutableListOf<Module>()

    fun resolve() {
        addTopLevelDefs()
        addImportedEntities()
        resolveTopLevelDefs()
    }

    private fun <T> MutableScope.DefinitionResult<T>.orThrow(name: Name): Def.Primary<T> =
        orThrow(name.name, name.location)

    private fun <T> MutableScope.DefinitionResult<T>.orThrow(name: String, location: Location?): Def.Primary<T> =
        when (this) {
            is MutableScope.DefinitionResult.Success -> this.def

            is MutableScope.DefinitionResult.Conflict -> throw ConflictingDefinitionException.fromName(
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
                        decl.primaryDef =
                            module.scope.define(decl.name.toString(), decl.name.location, decl).orThrow(decl.name)
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

                        is ModuleScope.ImportResult.Conflict -> throw ConflictingImportException(
                            decl.location,
                            "imported name `${def.name}` conflicts with a previous import",
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
        TODO("Not yet implemented")
    }
}
