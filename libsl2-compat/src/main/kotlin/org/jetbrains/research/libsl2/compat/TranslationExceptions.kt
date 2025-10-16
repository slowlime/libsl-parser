package org.jetbrains.research.libsl2.compat

import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.location.inAt

open class TranslationException(
    message: String,
    val location: Location?,
    cause: Throwable? = null,
) : Exception(message, cause)

class ExpectedNameTypeExprException(location: Location?) : TranslationException(
    "libsl1 requires a named type expression, possibly qualified with type arguments (${location.inAt()})",
    location,
)

class UnexpectedPointerTypeExprException(location: Location?) : TranslationException(
    "encountered an unexpected pointer type expression (${location.inAt()})",
    location,
)

class NonBmpCharException(location: Location?) : TranslationException(
    "libsl1 does not support non-BMP character literals (${location.inAt()})",
    location,
)

class CalleeNotNameAccessException(location: Location?) : TranslationException(
    "the callee must be a plain-name access expression (${location.inAt()})",
    location,
)

class GlobalProcDeclException(location: Location?) : TranslationException(
    "libsl1 does not support proc declaration at the top level (${location.inAt()})",
    location,
)

class ComplexPredicateException(location: Location?) : TranslationException(
    "libsl1 does not support non-expression predicates (${location.inAt()})",
    location,
)

class SetLitExprException(location: Location?) : TranslationException(
    "libsl1 does not support set literal expressions (${location.inAt()})",
    location,
)

class InExprException(location: Location?) : TranslationException(
    "libsl1 does not support `in`-expressions (${location.inAt()})",
    location,
)

class CancelStmtException(location: Location?) : TranslationException(
    "libsl1 does not support `cancel` statements (${location.inAt()})",
    location,
)
