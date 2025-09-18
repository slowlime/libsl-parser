package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

data class Header(
    var location: Location,
    var libslVersion: String,
    var libraryName: String,
    var version: String?,
    var language: String?,
    var url: String?,
)
