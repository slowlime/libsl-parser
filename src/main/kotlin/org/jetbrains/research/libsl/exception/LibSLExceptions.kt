package org.jetbrains.research.libsl.exception

import org.jetbrains.research.libsl.location.Location
import org.jetbrains.research.libsl.location.inAt

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
