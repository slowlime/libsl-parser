package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.location.Location

data class Header(
    var location: Location,
    var libslVersion: String,
    var libraryName: String,
    var version: String?,
    var language: String?,
    var url: String?,
)
