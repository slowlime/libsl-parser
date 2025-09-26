package org.jetbrains.research.libsl.resolve.scope

import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.ast.decl.ActionDecl
import org.jetbrains.research.libsl.ast.decl.AnnotationDecl
import org.jetbrains.research.libsl.ast.decl.AutomatonDecl
import org.jetbrains.research.libsl.ast.decl.FunctionLikeDecl
import org.jetbrains.research.libsl.ast.decl.StateDecl
import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.resolve.Binding
import org.jetbrains.research.libsl.resolve.Def
import org.jetbrains.research.libsl.type.AnyType
import org.jetbrains.research.libsl.type.BoolType
import org.jetbrains.research.libsl.type.CharType
import org.jetbrains.research.libsl.type.FloatType
import org.jetbrains.research.libsl.type.IntType
import org.jetbrains.research.libsl.type.NothingType
import org.jetbrains.research.libsl.type.StringType
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.type.TypeConstructor
import org.jetbrains.research.libsl.type.VoidType

abstract class Scope(val parent: Scope?, val resolutionParent: Scope? = parent) {
    abstract fun resolveTypeLocally(name: String): Def<TypeConstructor>?
    abstract fun resolveAutomatonLocally(name: String): Def<AutomatonDecl>?
    abstract fun resolveFunctionLocally(name: String): List<Def<FunctionLikeDecl>>
    abstract fun resolveBindingLocally(name: String): Def<Binding>?
    abstract fun resolveAnnotationLocally(name: String): Def<AnnotationDecl>?
    abstract fun resolveActionLocally(name: String): Def<ActionDecl>?
    abstract fun resolveStateLocally(name: String): Def<StateDecl>?

    protected open fun <T> resolveRecursively(name: String, resolveLocally: Scope.(String) -> T?): T? =
        resolveLocally(name) ?: resolutionParent?.resolveRecursively(name, resolveLocally)

    fun resolveType(name: String): Def<TypeConstructor>? =
        resolveRecursively(name, Scope::resolveTypeLocally)

    fun resolveAutomaton(name: String): Def<AutomatonDecl>? =
        resolveRecursively(name, Scope::resolveAutomatonLocally)

    fun resolveBinding(name: String): Def<Binding>? =
        resolveRecursively(name, Scope::resolveBindingLocally)

    fun resolveAnnotation(name: String): Def<AnnotationDecl>? =
        resolveRecursively(name, Scope::resolveAnnotationLocally)

    fun resolveAction(name: String): Def<ActionDecl>? =
        resolveRecursively(name, Scope::resolveActionLocally)

    fun resolveState(name: String): Def<StateDecl>? =
        resolveRecursively(name, Scope::resolveStateLocally)
}

open class MutableScope(parent: Scope?, resolutionParent: Scope? = parent) : Scope(parent, resolutionParent) {
    val types = mutableMapOf<String, Def<TypeConstructor>>()
    val automata = mutableMapOf<String, Def<AutomatonDecl>>()
    val functions = mutableMapOf<String, MutableList<Def<FunctionLikeDecl>>>()
    val bindings = mutableMapOf<String, Def<Binding>>()
    val annotations = mutableMapOf<String, Def<AnnotationDecl>>()
    val actions = mutableMapOf<String, Def<ActionDecl>>()
    val states = mutableMapOf<String, Def<StateDecl>>()

    override fun resolveTypeLocally(name: String): Def<TypeConstructor>? = types[name]

    override fun resolveAutomatonLocally(name: String): Def<AutomatonDecl>? = automata[name]

    override fun resolveFunctionLocally(name: String): MutableList<Def<FunctionLikeDecl>> =
        functions[name] ?: mutableListOf()

    override fun resolveBindingLocally(name: String): Def<Binding>? = bindings[name]

    override fun resolveAnnotationLocally(name: String): Def<AnnotationDecl>? = annotations[name]

    override fun resolveActionLocally(name: String): Def<ActionDecl>? = actions[name]

    override fun resolveStateLocally(name: String): Def<StateDecl>? = states[name]

    sealed interface DefinitionResult<T> {
        data class Success<T>(val def: Def.Primary<T>) : DefinitionResult<T>
        data class Conflict<T>(val previousDef: Def<T>) : DefinitionResult<T>
    }

    sealed interface AliasResult<T> {
        data class Success<T>(val def: Def.Alias<T>, val new: Boolean) : AliasResult<T>
        data class Conflict<T>(val previousDef: Def<T>) : AliasResult<T>
    }

    private inline fun <T, D : Def<T>, R> MutableMap<String, Def<T>>.addDef(
        def: D,
        onSuccess: (D) -> R,
        onIgnored: (Def.Alias<T>) -> R,
        onConflict: (Def<T>) -> R,
    ): R {
        return when (val previousDef = get(def.name)) {
            null -> {
                put(def.name, def)

                onSuccess(def)
            }

            // as long as they resolve to the same primary def, we ignore duplicate aliases
            is Def.Alias if def.isAlias() && previousDef.primary === def.primary -> {
                onIgnored(previousDef)
            }

            else -> {
                onConflict(previousDef)
            }
        }
    }

    private fun <T> MutableMap<String, Def<T>>.define(
        name: String,
        location: Location?,
        entity: T,
    ): DefinitionResult<T> = addDef(
        Def.Primary(this@MutableScope, name, location, entity),
        onSuccess = { DefinitionResult.Success(it) },
        onIgnored = { error("primary defs cannot get ignored") },
        onConflict = { DefinitionResult.Conflict(it) },
    )

    private fun <T> MutableMap<String, Def<T>>.alias(name: String, location: Location?, def: Def<T>): AliasResult<T> =
        addDef(
            Def.Alias(this@MutableScope, name, location, def),
            onSuccess = { AliasResult.Success(it, new = true) },
            onIgnored = { AliasResult.Success(it, new = false) },
            onConflict = { AliasResult.Conflict(it) },
        )

    fun define(name: String, location: Location?, type: TypeConstructor): DefinitionResult<TypeConstructor> =
        types.define(name, location, type)

    fun define(name: String, location: Location?, decl: AutomatonDecl): DefinitionResult<AutomatonDecl> =
        automata.define(name, location, decl)

    fun define(name: String, location: Location?, decl: FunctionLikeDecl): DefinitionResult.Success<FunctionLikeDecl> {
        val overloads = functions.getOrPut(name, ::mutableListOf)
        val def = Def.Primary(this, name, location, decl)
        overloads += def

        return DefinitionResult.Success(def)
    }

    fun define(name: String, location: Location?, decl: Binding): DefinitionResult<Binding> =
        bindings.define(name, location, decl)

    fun define(name: String, location: Location?, decl: AnnotationDecl): DefinitionResult<AnnotationDecl> =
        annotations.define(name, location, decl)

    fun define(name: String, location: Location?, decl: ActionDecl): DefinitionResult<ActionDecl> =
        actions.define(name, location, decl)

    fun define(name: String, location: Location?, decl: StateDecl): DefinitionResult<StateDecl> =
        states.define(name, location, decl)

    fun aliasType(name: String, location: Location?, def: Def<TypeConstructor>): AliasResult<TypeConstructor> =
        types.alias(name, location, def)

    fun aliasAutomaton(name: String, location: Location?, def: Def<AutomatonDecl>): AliasResult<AutomatonDecl> =
        automata.alias(name, location, def)

    fun aliasFunction(
        name: String,
        location: Location?,
        def: Def<FunctionLikeDecl>,
    ): AliasResult.Success<FunctionLikeDecl> {
        val overloads = functions.getOrPut(name, ::mutableListOf)
        val def = Def.Alias(this, name, location, def)

        // TODO: linear scan is performed here, which can be improved
        val existingDef = overloads.asSequence()
            .mapNotNull { it as? Def.Alias }
            .find { it.primary === def.primary }

        return if (existingDef == null) {
            overloads += def

            AliasResult.Success(def, new = true)
        } else {
            AliasResult.Success(existingDef, new = false)
        }
    }

    fun aliasBinding(name: String, location: Location?, def: Def<Binding>): AliasResult<Binding> =
        bindings.alias(name, location, def)

    fun aliasAnnotation(name: String, location: Location?, def: Def<AnnotationDecl>): AliasResult<AnnotationDecl> =
        annotations.alias(name, location, def)

    fun aliasAction(name: String, location: Location?, def: Def<ActionDecl>): AliasResult<ActionDecl> =
        actions.alias(name, location, def)
}

object GlobalScope : Scope(null, null) {
    private val types = buildMap {
        fun put(name: String, type: Type) {
            put(name, Def.Primary(GlobalScope, name, location = null, TypeConstructor.Nullary(type)))
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

    override fun resolveTypeLocally(name: String): Def<TypeConstructor>? = types[name]
    override fun resolveAutomatonLocally(name: String): Def<AutomatonDecl>? = null
    override fun resolveFunctionLocally(name: String): List<Def<FunctionLikeDecl>> = listOf()
    override fun resolveBindingLocally(name: String): Def<Binding>? = null
    override fun resolveAnnotationLocally(name: String): Def<AnnotationDecl>? = null
    override fun resolveActionLocally(name: String): Def<ActionDecl>? = null
    override fun resolveStateLocally(name: String): Def<StateDecl>? = null
}

class ModuleScope private constructor(
    val importScope: MutableScope,
    val module: Module,
) : MutableScope(GlobalScope, importScope) {
    constructor(module: Module) : this(MutableScope(GlobalScope), module)

    sealed interface ImportResult<T> {
        data class Success<T>(val def: Def.Alias<T>, val new: Boolean) : ImportResult<T>
        data class Conflict<T>(val previousDef: Def.Alias<T>, val previousDefModule: Module) : ImportResult<T>
    }

    private fun <T> AliasResult<T>.toImportResult(): ImportResult<T> = when (this) {
        is AliasResult.Success -> ImportResult.Success(this.def, this.new)

        is AliasResult.Conflict -> {
            val previousDef = this.previousDef as Def.Alias<T>
            val previousDefScope = previousDef.def.scope as ModuleScope

            ImportResult.Conflict(previousDef, previousDefScope.module)
        }
    }

    private fun <T> import(
        alias: ModuleScope.(String, Location?, Def<T>) -> AliasResult<T>,
        location: Location?,
        def: Def<T>,
    ): ImportResult<T> {
        require(def.scope is ModuleScope) { "imported def must come from a module scope" }
        val name = def.name

        return alias(name, location, def).toImportResult()
    }

    fun importType(location: Location?, def: Def<TypeConstructor>): ImportResult<TypeConstructor> =
        import(ModuleScope::aliasType, location, def)

    fun importAutomaton(location: Location?, def: Def<AutomatonDecl>): ImportResult<AutomatonDecl> =
        import(ModuleScope::aliasAutomaton, location, def)

    fun importFunction(location: Location?, def: Def<FunctionLikeDecl>): ImportResult<FunctionLikeDecl> =
        import(ModuleScope::aliasFunction, location, def)

    fun importBinding(location: Location?, def: Def<Binding>): ImportResult<Binding> =
        import(ModuleScope::aliasBinding, location, def)

    fun importAnnotation(location: Location?, def: Def<AnnotationDecl>): ImportResult<AnnotationDecl> =
        import(ModuleScope::aliasAnnotation, location, def)

    fun importAction(location: Location?, def: Def<ActionDecl>): ImportResult<ActionDecl> =
        import(ModuleScope::aliasAction, location, def)

    private fun <T> mergeScopes(getNamespace: MutableScope.() -> MutableMap<String, Def<T>>): Sequence<Def<T>> {
        val importedDefs = importScope.getNamespace().values.asSequence()
            .filterNot { getNamespace().containsKey(it.name) }
        val localDefs = getNamespace().values.asSequence()

        return importedDefs + localDefs
    }

    val allTypes: Sequence<Def<TypeConstructor>>
        get() = mergeScopes { types }

    val allAutomata: Sequence<Def<AutomatonDecl>>
        get() = mergeScopes { automata }

    val allFunctions: Sequence<Def<FunctionLikeDecl>>
        get() {
            val keys = importScope.functions.keys + functions.keys

            return keys.asSequence()
                .flatMap { name ->
                    val imported = importScope.functions[name].orEmpty()
                    val local = functions[name].orEmpty()

                    imported + local
                }
        }

    val allBindings: Sequence<Def<Binding>>
        get() = mergeScopes { bindings }

    val allAnnotations: Sequence<Def<AnnotationDecl>>
        get() = mergeScopes { annotations }

    val allActions: Sequence<Def<ActionDecl>>
        get() = mergeScopes { actions }
}
