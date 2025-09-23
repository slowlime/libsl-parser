package org.jetbrains.research.libsl.exception

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.location.Location

open class LibSLException(message: String, cause: Throwable?) : Exception(message, cause)

open class LocalizedException(
    val location: Location?,
    message: String,
    cause: Throwable? = null,
) : LibSLException(message, cause)

class IllegalSyntaxException(
    location: Location?,
    message: String,
    cause: Throwable?,
) : LocalizedException(location, message, cause)

class ConflictingDefinitionException(
    location: Location?,
    message: String,
    val previousDefLocation: Location?,
) : LocalizedException(location, message) {
    companion object {
        fun fromName(name: Name, previousDefLocation: Location?): ConflictingDefinitionException =
            fromName(name.name, name.location, previousDefLocation)

        fun fromName(
            name: String,
            location: Location?,
            previousDefLocation: Location?,
        ): ConflictingDefinitionException = ConflictingDefinitionException(
            location,
            "name `$name` conflicts with a previous definition",
            previousDefLocation,
        )
    }
}
