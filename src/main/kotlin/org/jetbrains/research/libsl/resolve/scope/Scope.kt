package org.jetbrains.research.libsl.resolve.scope

import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.ast.decl.ActionDecl
import org.jetbrains.research.libsl.ast.decl.AnnotationDecl
import org.jetbrains.research.libsl.ast.decl.AutomatonDecl
import org.jetbrains.research.libsl.ast.decl.FunctionDecl
import org.jetbrains.research.libsl.ast.decl.VariableDecl
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.type.AnyType
import org.jetbrains.research.libsl.type.BoolType
import org.jetbrains.research.libsl.type.CharType
import org.jetbrains.research.libsl.type.FloatType
import org.jetbrains.research.libsl.type.IntType
import org.jetbrains.research.libsl.type.NothingType
import org.jetbrains.research.libsl.type.StringType
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.type.VoidType

abstract class Scope(val parent: Scope?, val resolutionParent: Scope? = parent) {
    abstract fun resolveTypeLocally(name: String): Def<Type>?
    abstract fun resolveAutomatonLocally(name: String): Def<AutomatonDecl>?
    abstract fun resolveFunctionLocally(name: String): Def<FunctionDecl>?
    abstract fun resolveVariableLocally(name: String): Def<VariableDecl>?
    abstract fun resolveAnnotationLocally(name: String): Def<AnnotationDecl>?
    abstract fun resolveActionLocally(name: String): Def<ActionDecl>?

    protected open fun <T> resolveRecursively(name: String, resolveLocally: Scope.(String) -> T?): T? =
        resolveLocally(name) ?: resolutionParent?.resolveRecursively(name, resolveLocally)

    fun resolveType(name: String): Def<Type>? = resolveRecursively(name, Scope::resolveTypeLocally)
    fun resolveAutomaton(name: String): Def<AutomatonDecl>? = resolveRecursively(name, Scope::resolveAutomatonLocally)
    fun resolveFunction(name: String): Def<FunctionDecl>? = resolveRecursively(name, Scope::resolveFunctionLocally)
    fun resolveVariable(name: String): Def<VariableDecl>? = resolveRecursively(name, Scope::resolveVariableLocally)
    fun resolveAnnotation(name: String): Def<AnnotationDecl>? =
        resolveRecursively(name, Scope::resolveAnnotationLocally)

    fun resolveAction(name: String): Def<ActionDecl>? = resolveRecursively(name, Scope::resolveActionLocally)
}

open class MutableScope(parent: Scope?, resolutionParent: Scope? = parent) : Scope(parent, resolutionParent) {
    val types = mutableMapOf<String, Def<Type>>()
    val automata = mutableMapOf<String, Def<AutomatonDecl>>()
    val functions = mutableMapOf<String, Def<FunctionDecl>>()
    val variables = mutableMapOf<String, Def<VariableDecl>>()
    val annotations = mutableMapOf<String, Def<AnnotationDecl>>()
    val actions = mutableMapOf<String, Def<ActionDecl>>()

    override fun resolveTypeLocally(name: String): Def<Type>? = types[name]
    override fun resolveAutomatonLocally(name: String): Def<AutomatonDecl>? = automata[name]
    override fun resolveFunctionLocally(name: String): Def<FunctionDecl>? = functions[name]
    override fun resolveVariableLocally(name: String): Def<VariableDecl>? = variables[name]
    override fun resolveAnnotationLocally(name: String): Def<AnnotationDecl>? = annotations[name]
    override fun resolveActionLocally(name: String): Def<ActionDecl>? = actions[name]

    sealed interface DefinitionResult<T> {
        data class Success<T>(val def: Def<T>) : DefinitionResult<T>
        data class Conflict<T>(val previousDef: Def<T>) : DefinitionResult<T>
    }

    private fun <T> MutableMap<String, Def<T>>.define(name: String, def: Def<T>): DefinitionResult<T> {
        val previousDef = get(name)

        return if (
            previousDef == null
            // aliases can override previous aliases as long as they resolve to the same primary def
            || previousDef.isAlias() && def.isAlias() && previousDef.primary === def.primary
        ) {
            put(name, def)

            DefinitionResult.Success(def)
        } else {
            DefinitionResult.Conflict(previousDef)
        }
    }

    private fun <T> MutableMap<String, Def<T>>.define(name: String, entity: T): DefinitionResult<T> =
        define(name, Def.Primary(this@MutableScope, name, entity))

    fun define(name: String, type: Type): DefinitionResult<Type> = types.define(name, type)
    fun define(name: String, decl: AutomatonDecl): DefinitionResult<AutomatonDecl> = automata.define(name, decl)
    fun define(name: String, decl: FunctionDecl): DefinitionResult<FunctionDecl> = functions.define(name, decl)
    fun define(name: String, decl: VariableDecl): DefinitionResult<VariableDecl> = variables.define(name, decl)
    fun define(name: String, decl: AnnotationDecl): DefinitionResult<AnnotationDecl> = annotations.define(name, decl)
    fun define(name: String, decl: ActionDecl): DefinitionResult<ActionDecl> = actions.define(name, decl)

    fun alias(name: String, def: Def<Type>): DefinitionResult<Type> = types.define(name, Def.Alias(this, name, def))

    fun alias(name: String, def: Def<AutomatonDecl>): DefinitionResult<AutomatonDecl> =
        automata.define(name, Def.Alias(this, name, def))

    fun alias(name: String, def: Def<FunctionDecl>): DefinitionResult<FunctionDecl> =
        functions.define(name, Def.Alias(this, name, def))

    fun alias(name: String, def: Def<VariableDecl>): DefinitionResult<VariableDecl> =
        variables.define(name, Def.Alias(this, name, def))

    fun alias(name: String, def: Def<AnnotationDecl>): DefinitionResult<AnnotationDecl> =
        annotations.define(name, Def.Alias(this, name, def))

    fun alias(name: String, def: Def<ActionDecl>): DefinitionResult<ActionDecl> =
        actions.define(name, Def.Alias(this, name, def))
}

object GlobalScope : Scope(null, null) {
    private val types = buildMap {
        fun put(name: String, type: Type) {
            put(name, Def.Primary(GlobalScope, name, type))
        }

        put("int8", IntType(IntType.Width.I8, signed = true))
        put("int16", IntType(IntType.Width.I16, signed = true))
        put("int32", IntType(IntType.Width.I32, signed = true))
        put("int64", IntType(IntType.Width.I64, signed = true))

        put("unsigned8", IntType(IntType.Width.I8, signed = false))
        put("unsigned16", IntType(IntType.Width.I16, signed = false))
        put("unsigned32", IntType(IntType.Width.I32, signed = false))
        put("unsigned64", IntType(IntType.Width.I64, signed = false))

        put("float32", FloatType(FloatType.Width.F32))
        put("float64", FloatType(FloatType.Width.F64))

        put("bool", BoolType)
        put("char", CharType)
        put("string", StringType)
        put("void", VoidType)

        put("any", AnyType)
        put("nothing", NothingType)
    }

    override fun resolveTypeLocally(name: String): Def<Type>? = types[name]
    override fun resolveAutomatonLocally(name: String): Def<AutomatonDecl>? = null
    override fun resolveFunctionLocally(name: String): Def<FunctionDecl>? = null
    override fun resolveVariableLocally(name: String): Def<VariableDecl>? = null
    override fun resolveAnnotationLocally(name: String): Def<AnnotationDecl>? = null
    override fun resolveActionLocally(name: String): Def<ActionDecl>? = null
}

class ModuleScope private constructor(
    val importScope: MutableScope,
    val module: Module,
) : MutableScope(GlobalScope, importScope) {
    constructor(module: Module) : this(MutableScope(GlobalScope), module)

    sealed interface ImportResult<T> {
        data class Success<T>(val def: Def.Alias<T>) : ImportResult<T>
        data class Conflict<T>(val previousDef: Def.Alias<T>, val previousDefModule: Module) : ImportResult<T>
    }

    private fun <T> DefinitionResult<T>.toImportResult(): ImportResult<T> = when (this) {
        is DefinitionResult.Success -> ImportResult.Success(this.def as Def.Alias)

        is DefinitionResult.Conflict -> {
            val previousDef = this.previousDef as Def.Alias
            val previousDefScope = previousDef.def.scope as ModuleScope

            ImportResult.Conflict(previousDef, previousDefScope.module)
        }
    }

    private fun <T> import(alias: ModuleScope.(String, Def<T>) -> DefinitionResult<T>, def: Def<T>): ImportResult<T> {
        require(def.scope is ModuleScope) { "imported def must come from a module scope" }
        val name = def.name

        return alias(name, def).toImportResult()
    }

    fun import(def: Def<Type>): ImportResult<Type> = import(ModuleScope::alias, def)
    fun import(def: Def<AutomatonDecl>): ImportResult<AutomatonDecl> = import(ModuleScope::alias, def)
    fun import(def: Def<FunctionDecl>): ImportResult<FunctionDecl> = import(ModuleScope::alias, def)
    fun import(def: Def<VariableDecl>): ImportResult<VariableDecl> = import(ModuleScope::alias, def)
    fun import(def: Def<AnnotationDecl>): ImportResult<AnnotationDecl> = import(ModuleScope::alias, def)
    fun import(def: Def<ActionDecl>): ImportResult<ActionDecl> = import(ModuleScope::alias, def)
}
