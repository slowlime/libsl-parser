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
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.scope.MutableScope
import org.jetbrains.research.libsl.type.AliasType
import org.jetbrains.research.libsl.type.EnumType
import org.jetbrains.research.libsl.type.SemanticType
import org.jetbrains.research.libsl.type.StructType

internal class ModuleResolver(private val libsl: LibSL, rootModule: Module) {
    private val modules = mutableListOf(rootModule)

    fun resolve() {
        addTopLevelDefs()
        addImportedEntities()
        resolveTopLevelDefs()
    }

    private inline fun forEachModule(f: (Module) -> Unit) {
        // this code is somewhat non-idiomatic to permit the module list to grow during iteration
        var i = 0

        while (i < modules.size) {
            f(modules[i])
            i += 1
        }
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
        val discoveredModules = modules.toMutableSet()

        forEachModule { module ->
            for (decl in module.decls) {
                when (decl) {
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
                            modules += decl.importedModule
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
        }
    }

    private fun addImportedEntities() {
        TODO("Not yet implemented")
    }

    private fun resolveTopLevelDefs() {
        TODO("Not yet implemented")
    }
}
