package org.jetbrains.research.libsl2.exception

import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.location.inAt

open class LibSLException(message: String, cause: Throwable?) : Exception(message, cause)

open class LocalizedException(
    val location: Location?,
    val shortMessage: String,
    val fullMessage: String,
    cause: Throwable? = null,
) : LibSLException(fullMessage, cause)

class IllegalSyntaxException(
    location: Location?,
    shortMessage: String,
    cause: Throwable?,
) : LocalizedException(location, shortMessage, "encountered a syntax error ${location.inAt()}: $shortMessage", cause)

class ConflictingDefinitionException(
    location: Location?,
    shortMessage: String,
    fullMessage: String,
    val previousDefLocation: Location?,
) : LocalizedException(location, shortMessage, fullMessage) {
    companion object {
        fun fromName(
            name: String,
            location: Location?,
            previousDefLocation: Location?,
        ): ConflictingDefinitionException = ConflictingDefinitionException(
            location,
            "name `$name` conflicts with a previous definition",
            "name `$name` (${location.inAt()}) conflicts with a previous definition (${previousDefLocation.inAt()})",
            previousDefLocation,
        )
    }
}

class ConflictingImportException(
    location: Location?,
    shortMessage: String,
    fullMessage: String,
    val importedEntityLocation: Location?,
    val previousImportLocation: Location?,
    val previousImportedEntityLocation: Location?,
) : LocalizedException(location, shortMessage, fullMessage) {
    companion object {
        fun fromName(
            name: String,
            importLocation: Location?,
            importedEntityLocation: Location?,
            previousImportLocation: Location?,
            previousImportedEntityLocation: Location?,
        ): ConflictingImportException = ConflictingImportException(
            importLocation,
            "imported name `$name` conflicts with a previous import",
            "name `$name` (imported ${importLocation.inAt()}; refers to ${importedEntityLocation.inAt()}) conflicts with a previous import (${previousImportLocation.inAt()}; refers to ${previousImportedEntityLocation.inAt()})",
            importedEntityLocation,
            previousImportLocation,
            previousImportedEntityLocation,
        )
    }
}

class ConflictingParamNameException(
    location: Location?,
    shortMessage: String,
    fullMessage: String,
    val previousLocation: Location?,
) : LocalizedException(location, shortMessage, fullMessage) {
    companion object {
        fun fromName(
            name: String,
            location: Location?,
            previousLocation: Location?,
        ): ConflictingParamNameException = ConflictingParamNameException(
            location,
            "parameter `$name` is defined multiple times",
            "parameter `$name` (${location.inAt()}) conflicts with a previous parameter (${previousLocation.inAt()})",
            previousLocation,
        )
    }
}

class UnresolvedReferenceException(
    location: Location?,
    shortMessage: String,
    fullMessage: String,
    val kind: Kind,
) : LocalizedException(location, shortMessage, fullMessage) {
    enum class Kind {
        Automaton,
        TypeParam,
        State,
        Annotation,
        Param,
        Type,
        Action,
        Binding,
    }

    companion object {
        fun toAutomaton(name: String, location: Location?): UnresolvedReferenceException = UnresolvedReferenceException(
            location,
            "unresolved automaton reference `$name`",
            "unresolved automaton reference `$name` (${location.inAt()})",
            Kind.Automaton,
        )

        fun toTypeParamInConstraint(name: String, location: Location?): UnresolvedReferenceException =
            UnresolvedReferenceException(
                location,
                "`$name` does not resolve to a type parameter",
                "`$name` (${location.inAt()}) does not resolve to a type parameter",
                Kind.TypeParam,
            )

        fun toState(name: String, location: Location?): UnresolvedReferenceException = UnresolvedReferenceException(
            location,
            "state `$name` has not been declared",
            "state `$name` (${location.inAt()}) has not been declared in this automaton",
            Kind.State,
        )

        fun toAnnotation(name: String, location: Location?): UnresolvedReferenceException =
            UnresolvedReferenceException(
                location,
                "unresolved annotation reference `$name`",
                "unresolved annotation reference `$name` (${location.inAt()})",
                Kind.Annotation,
            )

        fun toParam(name: String, location: Location?): UnresolvedReferenceException = UnresolvedReferenceException(
            location,
            "unresolved reference to param `$name`",
            "unresolved reference to param `$name` (${location.inAt()})",
            Kind.Param,
        )

        fun toType(name: String, location: Location?): UnresolvedReferenceException = UnresolvedReferenceException(
            location,
            "unresolved reference to type `$name`",
            "unresolved reference to type `$name` (${location.inAt()})",
            Kind.Type,
        )

        fun toAction(name: String, location: Location?): UnresolvedReferenceException = UnresolvedReferenceException(
            location,
            "unresolved reference to action `$name`",
            "unresolved reference to action `$name` (${location.inAt()})",
            Kind.Action,
        )

        fun toBinding(name: String, location: Location?): UnresolvedReferenceException = UnresolvedReferenceException(
            location,
            "unresolved reference to variable `$name`",
            "unresolved reference to variable `$name` (${location.inAt()})",
            Kind.Binding,
        )
    }
}

class UnorderedMixedNamedAndUnnamedArgumentsException(
    location: Location?,
    shortMessage: String,
    fullMessage: String,
) : LocalizedException(location, shortMessage, fullMessage) {
    constructor(location: Location?) : this(
        location,
        "cannot mix named and unnamed arguments unless they are in the right order",
        "cannot mix named and unnamed arguments unless they are in the right order (${location.inAt()})",
    )
}

class TooManyArgumentsException(
    location: Location?,
    shortMessage: String,
    fullMessage: String,
    val argCount: Int,
    val paramCount: Int,
) : LocalizedException(location, shortMessage, fullMessage) {
    constructor(location: Location?, argCount: Int, paramCount: Int) : this(
        location,
        "too many arguments: expected $paramCount, found $argCount",
        "too many arguments: expected $paramCount, found $argCount (${location.inAt()})",
        argCount,
        paramCount,
    )
}

class TooFewArgumentsException(
    location: Location?,
    shortMessage: String,
    fullMessage: String,
    val argCount: Int,
    val paramCount: Int,
) : LocalizedException(location, shortMessage, fullMessage) {
    constructor(location: Location?, argCount: Int, paramCount: Int) : this(
        location,
        "too few arguments: expected $paramCount, found $argCount",
        "too few arguments: expected $paramCount, found $argCount (${location.inAt()})",
        argCount,
        paramCount,
    )
}

class TooManyTypeArgumentsException(
    location: Location?,
    shortMessage: String,
    fullMessage: String,
    val argCount: Int,
    val paramCount: Int,
) : LocalizedException(location, shortMessage, fullMessage) {
    constructor(location: Location?, argCount: Int, paramCount: Int) : this(
        location,
        "too many type arguments: expected $paramCount, found $argCount",
        "too many type arguments: expected $paramCount, found $argCount (${location.inAt()})",
        argCount,
        paramCount,
    )
}

class TooFewTypeArgumentsException(
    location: Location?,
    shortMessage: String,
    fullMessage: String,
    val argCount: Int,
    val paramCount: Int,
) : LocalizedException(location, shortMessage, fullMessage) {
    constructor(location: Location?, argCount: Int, paramCount: Int) : this(
        location,
        "too few type arguments: expected $paramCount, found $argCount",
        "too few type arguments: expected $paramCount, found $argCount (${location.inAt()})",
        argCount,
        paramCount,
    )
}
