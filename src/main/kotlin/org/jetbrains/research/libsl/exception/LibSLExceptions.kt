package org.jetbrains.research.libsl.exception

import org.jetbrains.research.libsl.location.Location

open class LibSLException(message: String, cause: Throwable?) : Exception(message, cause)

open class LocalizedException(
    val location: Location,
    message: String,
    cause: Throwable?,
) : LibSLException(message, cause)

class IllegalSyntaxException(
    location: Location,
    message: String,
    cause: Throwable?,
) : LocalizedException(location, message, cause)
